package unit.billing_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.billing.application.command.GenerateInvoiceCommand;
import salon.billing.application.domain.event.ErrorDuringInvoiceCreationEvent;
import salon.billing.application.domain.event.InvoiceCreatedEvent;
import salon.billing.application.domain.exception.DocumentGenerationException;
import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.domain.model.document.AccountingDocumentFactory;
import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.service.InvoiceCalculationService;
import salon.billing.application.port.out.DocumentDatabaseRepository;
import salon.billing.application.port.out.NotificationGeneration;
import salon.billing.application.port.out.PdfGeneration;
import salon.billing.application.port.out.SalesIntegration;
import salon.billing.application.port.out.SettlementDatabaseRepository;
import salon.billing.application.service.DocumentGenerationService;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-FIR-02: Stworzenie faktury końcowej — serwis DocumentGenerationService. */
@ExtendWith(MockitoExtension.class)
class GenerateInvoiceAppServiceTest {

    @Mock private SettlementDatabaseRepository settlementRepository;
    @Mock private DocumentDatabaseRepository documentRepository;
    @Mock private PdfGeneration pdfGeneration;
    @Mock private NotificationGeneration notification;
    @Mock private EventPublisher eventPublisher;
    @Mock private SalesIntegration salesIntegration;

    private DocumentGenerationService service;
    private final SettlementFactory settlementFactory = new SettlementFactory();
    private final SellerDetails seller = new SellerDetails("Salon Sp. z o.o.", "5260000000");

    @BeforeEach
    void setUp() {
        service = new DocumentGenerationService(settlementRepository, documentRepository,
                new InvoiceCalculationService(), new AccountingDocumentFactory(),
                pdfGeneration, notification, eventPublisher, salesIntegration, seller);
    }

    @Test
    void shouldCreateFinalInvoiceAndPublishEvent() { // SCENARIUSZ GŁÓWNY
        Settlement settlement = settlementFactory.createNew(new OrderId("ORD-1"), Money.of(100000, "PLN"));
        when(settlementRepository.findByOrderId(new OrderId("ORD-1"))).thenReturn(Optional.of(settlement));
        when(salesIntegration.buyerDetailsFor("ORD-1")).thenReturn(new BuyerDetails("Firma S.A.", "1234563218"));
        when(pdfGeneration.generatePdf(any(AccountingDocument.class))).thenReturn(new byte[]{1, 2, 3});

        service.generateInvoice(new GenerateInvoiceCommand("ORD-1", "Faktura końcowa ORD-1", "FA-Księgowy"));

        // Faktura wystawiona i przypisana do zamówienia, emisja InvoiceCreated
        verify(documentRepository, atLeastOnce()).save(any(AccountingDocument.class));
        verify(notification).notifyInvoiceIssued(any(AccountingDocument.class), any());
        verify(eventPublisher).publish(any(InvoiceCreatedEvent.class));
    }

    @Test
    void shouldEmitErrorWhenPdfGenerationFails() { // Scenariusz alternatywny A1
        Settlement settlement = settlementFactory.createNew(new OrderId("ORD-2"), Money.of(100000, "PLN"));
        when(settlementRepository.findByOrderId(new OrderId("ORD-2"))).thenReturn(Optional.of(settlement));
        when(salesIntegration.buyerDetailsFor("ORD-2")).thenReturn(new BuyerDetails("Firma S.A.", "1234563218"));
        when(pdfGeneration.generatePdf(any(AccountingDocument.class)))
                .thenThrow(new DocumentGenerationException("Błąd generowania pliku"));

        // Błąd generowania dokumentu — proces zgłasza błąd i emituje ErrorDuringInvoiceCreation
        assertThatThrownBy(() -> service.generateInvoice(
                new GenerateInvoiceCommand("ORD-2", "Faktura końcowa ORD-2", "FA-Księgowy")))
                .isInstanceOf(DocumentGenerationException.class);
        verify(eventPublisher).publish(any(ErrorDuringInvoiceCreationEvent.class));
    }
}
