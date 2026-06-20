package salon.billing.application.service;

import salon.billing.application.command.GenerateAdvanceCommand;
import salon.billing.application.command.GenerateInvoiceCommand;
import salon.billing.application.domain.event.ErrorDuringInvoiceCreationEvent;
import salon.billing.application.domain.event.ErrorDuringPaymentRequestEvent;
import salon.billing.application.domain.event.InvoiceCreatedEvent;
import salon.billing.application.domain.exception.SettlementNotFoundException;
import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.domain.model.document.AccountingDocumentFactory;
import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.service.InvoiceCalculationService;
import salon.billing.application.port.in.GenerateAdvance;
import salon.billing.application.port.in.GenerateInvoice;
import salon.billing.application.port.out.DocumentDatabaseRepository;
import salon.billing.application.port.out.NotificationGeneration;
import salon.billing.application.port.out.PdfGeneration;
import salon.billing.application.port.out.SalesIntegration;
import salon.billing.application.port.out.SettlementDatabaseRepository;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * USLUGA APLIKACJI (Rys. 48 — DocumentGenerationService) — orkiestrator wystawiania dokumentow.
 *
 * Realizuje porty wejsciowe {@link GenerateAdvance} (UC-FIR-01) i {@link GenerateInvoice} (UC-FIR-02).
 * Koordynuje: usluge dziedziny {@link InvoiceCalculationService}, ACL Sprzedazy ({@link SalesIntegration}),
 * fabryke i agregat dokumentu, porty {@link PdfGeneration}/{@link NotificationGeneration}/{@link DocumentDatabaseRepository}
 * oraz publikacje zdarzen. Reguly biznesowe pozostaja w agregatach — usluga jedynie spina kroki PU.
 */
public class DocumentGenerationService implements GenerateAdvance, GenerateInvoice {

    private final SettlementDatabaseRepository settlementRepository;
    private final DocumentDatabaseRepository documentRepository;
    private final InvoiceCalculationService invoiceCalculation;
    private final AccountingDocumentFactory documentFactory;
    private final PdfGeneration pdfGeneration;
    private final NotificationGeneration notification;
    private final EventPublisher eventPublisher;
    private final SalesIntegration salesIntegration;
    private final SellerDetails seller;

    public DocumentGenerationService(SettlementDatabaseRepository settlementRepository,
                                     DocumentDatabaseRepository documentRepository,
                                     InvoiceCalculationService invoiceCalculation,
                                     AccountingDocumentFactory documentFactory,
                                     PdfGeneration pdfGeneration,
                                     NotificationGeneration notification,
                                     EventPublisher eventPublisher,
                                     SalesIntegration salesIntegration,
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
        if (pdfGeneration == null) {
            throw new IllegalArgumentException("pdfGeneration must not be null.");
        }
        if (notification == null) {
            throw new IllegalArgumentException("notification must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        if (salesIntegration == null) {
            throw new IllegalArgumentException("salesIntegration must not be null.");
        }
        if (seller == null) {
            throw new IllegalArgumentException("seller must not be null.");
        }
        this.settlementRepository = settlementRepository;
        this.documentRepository = documentRepository;
        this.invoiceCalculation = invoiceCalculation;
        this.documentFactory = documentFactory;
        this.pdfGeneration = pdfGeneration;
        this.notification = notification;
        this.eventPublisher = eventPublisher;
        this.salesIntegration = salesIntegration;
        this.seller = seller;
    }

    /**
     * UC-FIR-01: Wyslanie prosby o zadatek. Liczy kwote zadatku, dociaga dane nabywcy (ACL),
     * tworzy i wystawia proforme, powiadamia klienta, a nastepnie oznacza wymagalnosc zadatku
     * na saldzie (agregat emituje AdvancePaymentRequestedEvent — publikowane na koncu).
     */
    @Override
    public String generateAdvance(GenerateAdvanceCommand command) {
        try {
            Settlement settlement = loadSettlement(command.orderId());

            Money advance = this.invoiceCalculation.calculateAdvanceAmount(settlement);
            BuyerDetails buyer = this.salesIntegration.getBuyerDetails(command.orderId());

            AccountingDocument document = this.documentFactory.createInvoice(
                    settlement.getOrderId(), buyer, this.seller, advance,
                    "Prosba o zadatek " + command.orderId(), command.authorizedIssuer());
            String documentId = issueAndNotify(document);

            settlement.requestAdvancePayment();
            this.settlementRepository.save(settlement);

            this.eventPublisher.publishAll(settlement.pullDomainEvents());
            return documentId;
        } catch (RuntimeException e) {
            // A1: brak wymaganych informacji do wygenerowania prosby.
            this.eventPublisher.publish(
                    new ErrorDuringPaymentRequestEvent(command.orderId(), e.getMessage()));
            throw e;
        }
    }

    /**
     * UC-FIR-02: Stworzenie faktury koncowej na kwote pozostala do zaplaty (uwzglednia zadatek).
     * Tworzy i wystawia fakture, generuje PDF, powiadamia klienta i emituje InvoiceCreatedEvent.
     */
    @Override
    public String generateInvoice(GenerateInvoiceCommand command) {
        try {
            Settlement settlement = loadSettlement(command.orderId());

            Money outstanding = this.invoiceCalculation.calculateFinalInvoiceAmount(settlement);
            BuyerDetails buyer = this.salesIntegration.getBuyerDetails(command.orderId());

            AccountingDocument document = this.documentFactory.createInvoice(
                    settlement.getOrderId(), buyer, this.seller, outstanding,
                    command.invoiceTitle(), command.authorizedIssuer());
            String documentId = issueAndNotify(document);

            this.eventPublisher.publish(new InvoiceCreatedEvent(command.orderId()));
            return documentId;
        } catch (RuntimeException e) {
            // A1: blad generowania dokumentu (PDF/zapis).
            this.eventPublisher.publish(
                    new ErrorDuringInvoiceCreationEvent(command.orderId(), e.getMessage()));
            throw e;
        }
    }

    /** Wspolna sciezka: zapis DRAFT -> PDF -> markAsIssued -> zapis -> powiadomienie klienta. */
    private String issueAndNotify(AccountingDocument document) {
        this.documentRepository.save(document);
        byte[] pdf = this.pdfGeneration.generatePdf(document);
        document.markAsIssued();
        this.documentRepository.save(document);
        this.notification.notifyInvoiceIssued(document, pdf);
        return document.getId().value();
    }

    private Settlement loadSettlement(String orderId) {
        return this.settlementRepository.findByOrderId(new OrderId(orderId))
                .orElseThrow(() -> new SettlementNotFoundException(
                        "Brak otwartego salda dla zamowienia " + orderId));
    }
}
