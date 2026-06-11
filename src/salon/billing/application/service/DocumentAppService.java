package salon.billing.application.service;

import salon.billing.application.command.GenerateAdvanceCommand;
import salon.billing.application.port.in.GenerateAdvanceUseCase;
import salon.billing.application.command.GenerateInvoiceCommand;
import salon.billing.application.port.in.GenerateInvoiceUseCase;
import salon.billing.application.port.out.CrmIntegrationPort;
import salon.billing.application.port.out.DocumentRepository;
import salon.billing.application.port.out.NotificationPort;
import salon.billing.application.port.out.PdfGeneratorPort;
import salon.billing.application.port.out.SettlementRepository;
import salon.billing.domain.event.ErrorDuringInvoiceCreation;
import salon.billing.domain.event.ErrorDuringPaymentRequest;
import salon.billing.domain.exception.SettlementNotFoundException;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.AccountingDocumentFactory;
import salon.billing.domain.model.document.BuyerDetails;
import salon.billing.domain.model.document.SellerDetails;
import salon.billing.domain.model.settlement.Settlement;
import salon.billing.domain.service.InvoiceCalculationDomainService;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.util.UUID;

/**
 * Realizuje UC-FIR-01 i UC-FIR-02 (warstwa aplikacji — orkiestracja).
 *
 * Czysty orkiestrator: NIE zawiera instrukcji warunkowych biznesowych ani operacji matematycznych.
 * Matematykę księgową wykonuje InvoiceCalculationDomainService (na podstawie stanu Settlement),
 * walidację terminów płatności i konstrukcję agregatu — AccountingDocumentFactory. Serwis jedynie
 * koordynuje wywołania między domeną a portami wyjściowymi (repo, PDF, notyfikacje, magistrala).
 *
 * Dane nabywcy (BuyerDetails) dociągane są z Kontekstu Sprzedaży/CRM przez CrmIntegrationPort —
 * zdarzenia wyzwalające (np. VehicleReservedFromStock) niosą tylko orderId/VIN.
 */
public class DocumentAppService implements GenerateAdvanceUseCase, GenerateInvoiceUseCase {

    private final SettlementRepository settlementRepository;
    private final DocumentRepository documentRepository;
    private final InvoiceCalculationDomainService invoiceCalculation;
    private final AccountingDocumentFactory documentFactory;
    private final PdfGeneratorPort pdfGenerator;
    private final NotificationPort notification;
    private final EventPublisherPort eventPublisher;
    private final CrmIntegrationPort crmIntegration;
    private final SellerDetails seller;

    public DocumentAppService(SettlementRepository settlementRepository,
                              DocumentRepository documentRepository,
                              InvoiceCalculationDomainService invoiceCalculation,
                              AccountingDocumentFactory documentFactory,
                              PdfGeneratorPort pdfGenerator,
                              NotificationPort notification,
                              EventPublisherPort eventPublisher,
                              CrmIntegrationPort crmIntegration,
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

    private void publishEvents(salon.shared.event.AbstractAggregateRoot aggregate) {
        for (DomainEvent event : aggregate.pullDomainEvents()) {
            this.eventPublisher.publish(event);
        }
    }
}
