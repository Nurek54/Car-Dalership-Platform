package salon.billing.application.service;

import salon.billing.application.command.GenerateAdvanceCommand;
import salon.billing.application.port.in.GenerateAdvance;
import salon.billing.application.command.GenerateInvoiceCommand;
import salon.billing.application.port.in.GenerateInvoice;
import salon.billing.application.port.out.SalesIntegration;
import salon.billing.application.port.out.DocumentDatabaseRepository;
import salon.billing.application.port.out.NotificationGeneration;
import salon.billing.application.port.out.PdfGeneration;
import salon.billing.application.port.out.SettlementDatabaseRepository;
import salon.billing.application.domain.event.ErrorDuringInvoiceCreation;
import salon.billing.application.domain.event.ErrorDuringPaymentRequest;
import salon.billing.application.domain.exception.SettlementNotFoundException;
import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.domain.model.document.AccountingDocumentFactory;
import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.service.InvoiceCalculationService;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.time.Instant;
import java.util.UUID;

/**
 * Realizuje UC-FIR-01 i UC-FIR-02 (warstwa aplikacji — orkiestracja).
 *
 * Czysty orkiestrator: NIE zawiera instrukcji warunkowych biznesowych ani operacji matematycznych.
 * Matematykę księgową wykonuje InvoiceCalculationService (na podstawie stanu Settlement),
 * walidację terminów płatności i konstrukcję agregatu — AccountingDocumentFactory. Serwis jedynie
 * koordynuje wywołania między domeną a portami wyjściowymi (repo, PDF, notyfikacje, magistrala).
 *
 * Dane nabywcy (BuyerDetails) dociągane są z Kontekstu Sprzedaży/CRM przez SalesIntegration —
 * zdarzenia wyzwalające (np. VehicleReservedFromStock) niosą tylko orderId/VIN.
 */
public class DocumentGenerationService implements GenerateAdvance, GenerateInvoice {

    private final SettlementDatabaseRepository settlementRepository;
    private final DocumentDatabaseRepository documentRepository;
    private final InvoiceCalculationService invoiceCalculation;
    private final AccountingDocumentFactory documentFactory;
    private final PdfGeneration pdfGenerator;
    private final NotificationGeneration notification;
    private final EventPublisher eventPublisher;
    private final SalesIntegration crmIntegration;
    private final SellerDetails seller;

    public DocumentGenerationService(SettlementDatabaseRepository settlementRepository,
                              DocumentDatabaseRepository documentRepository,
                              InvoiceCalculationService invoiceCalculation,
                              AccountingDocumentFactory documentFactory,
                              PdfGeneration pdfGenerator,
                              NotificationGeneration notification,
                              EventPublisher eventPublisher,
                              SalesIntegration crmIntegration,
                              SellerDetails seller) {
        if (settlementRepository == null) {
            throw new IllegalArgumentException("settlementRepository must not be null.");
        }
        if (documentRepository == null) {
            throw new IllegalArgumentException("documentRepository must not be null.");
        }
        if (invoiceCalculation == null) {
            throw new IllegalArgumentException("invoiceCalculation must not be null.");
        }
        if (documentFactory == null) {
            throw new IllegalArgumentException("documentFactory must not be null.");
        }
        if (pdfGenerator == null) {
            throw new IllegalArgumentException("pdfGenerator must not be null.");
        }
        if (notification == null) {
            throw new IllegalArgumentException("notification must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        if (crmIntegration == null) {
            throw new IllegalArgumentException("crmIntegration must not be null.");
        }
        if (seller == null) {
            throw new IllegalArgumentException("seller must not be null.");
        }
        this.settlementRepository = settlementRepository;
        this.documentRepository = documentRepository;
        this.invoiceCalculation = invoiceCalculation;
        this.documentFactory = documentFactory;
        this.pdfGenerator = pdfGenerator;
        this.notification = notification;
        this.eventPublisher = eventPublisher;
        this.crmIntegration = crmIntegration;
        this.seller = seller;
    }

    @Override
    // UC-FIR-01: dokument zadatku (prośba o wpłatę z danymi do przelewu).
    public String generateAdvance(GenerateAdvanceCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        OrderId orderId = new OrderId(command.orderId());
        try {
            Settlement settlement = loadSettlement(orderId);

            Money advanceAmount = this.invoiceCalculation.calculateAdvanceAmount(settlement);
            BuyerDetails buyer = loadBuyerDetails(orderId);

            AccountingDocument document = this.documentFactory.create(
                    orderId, buyer, this.seller, advanceAmount,
                    "Zadatek - zamowienie " + orderId.value(), command.authorizedIssuer());

            AccountingDocument issued = issueAndDeliver(document);

            // UC-FIR-01: agregat salda żąda wpłaty zadatku -> zdarzenie dla reszty systemu.
            settlement.requestAdvancePayment();
            this.settlementRepository.save(settlement);
            publishEvents(settlement);

            return issued.getId().value();
        } catch (RuntimeException e) {
            // A1 — Błąd danych: brak wymaganych informacji do wygenerowania prośby.
            this.eventPublisher.publish(new ErrorDuringPaymentRequest(
                    UUID.randomUUID(), orderId.value(), e.getMessage(), Instant.now()));
            throw e;
        }
    }

    @Override
    // UC-FIR-02: faktura końcowa.
    public String generateInvoice(GenerateInvoiceCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        OrderId orderId = new OrderId(command.orderId());
        try {
            Settlement settlement = loadSettlement(orderId);

            Money finalAmount = this.invoiceCalculation.calculateFinalInvoiceAmount(settlement);
            BuyerDetails buyer = loadBuyerDetails(orderId);

            AccountingDocument document = this.documentFactory.create(
                    orderId, buyer, this.seller, finalAmount,
                    command.invoiceTitle(), command.authorizedIssuer());

            AccountingDocument issued = issueAndDeliver(document);
            return issued.getId().value();
        } catch (RuntimeException e) {
            // A1 — Błąd generowania dokumentu (PDF/zapis).
            this.eventPublisher.publish(new ErrorDuringInvoiceCreation(
                    UUID.randomUUID(), orderId.value(), e.getMessage(), Instant.now()));
            throw e;
        }
    }

    // UC-FIR-01/02: dane klienta z modułu Sprzedaży/CRM (zamiast "z powietrza" w komendzie).
    private BuyerDetails loadBuyerDetails(OrderId orderId) {
        BuyerDetails buyer = this.crmIntegration.getCustomerDetails(orderId);
        if (buyer == null) {
            throw new IllegalStateException("No customer details in CRM for order " + orderId.value());
        }
        return buyer;
    }

    private Settlement loadSettlement(OrderId orderId) {
        return this.settlementRepository.findByOrderId(orderId)
                .orElseThrow(() -> new SettlementNotFoundException(
                        "No settlement for order " + orderId.value()));
    }

    // Wspólny przepływ wyjściowy: zapis -> PDF -> wystawienie -> notyfikacja -> publikacja zdarzeń.
    private AccountingDocument issueAndDeliver(AccountingDocument document) {
        this.documentRepository.save(document);
        byte[] pdf = this.pdfGenerator.generatePdf(document);
        document.markAsIssued();
        this.documentRepository.save(document);
        this.notification.notifyInvoiceIssued(document, pdf);
        publishEvents(document);
        return document;
    }

    private void publishEvents(salon.common.event.AbstractAggregateRoot aggregate) {
        for (DomainEvent event : aggregate.pullDomainEvents()) {
            this.eventPublisher.publish(event);
        }
    }
}
