package acceptance.billing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import salon.billing.application.command.ProcessPaymentCommand;
import salon.billing.application.domain.event.AdvancePaymentRequestedEvent;
import salon.billing.application.domain.event.InvoiceCreatedEvent;
import salon.billing.application.domain.event.PaymentRegisteredEvent;
import salon.billing.application.domain.event.SettlementCompletedEvent;
import salon.billing.application.domain.model.document.AccountingDocumentFactory;
import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.model.settlement.SettlementStatus;
import salon.billing.application.domain.service.InvoiceCalculationService;
import salon.billing.application.port.in.GenerateAdvance;
import salon.billing.application.port.in.GenerateInvoice;
import salon.billing.application.port.in.ProcessPayment;
import salon.billing.application.port.out.DocumentDatabaseRepository;
import salon.billing.application.port.out.NotificationGeneration;
import salon.billing.application.port.out.PdfGeneration;
import salon.billing.application.port.out.SalesIntegration;
import salon.billing.application.port.out.SettlementDatabaseRepository;
import salon.billing.application.service.DocumentGenerationService;
import salon.billing.application.service.PaymentProcessService;
import salon.billing.infrastructure.in.messaging.BillingEventSubscriberAdapter;
import salon.billing.infrastructure.in.messaging.OrderReadyForSettlementEvent;
import salon.billing.infrastructure.in.messaging.SettlementEventListener;
import salon.billing.infrastructure.out.mock.InMemoryDocumentRepository;
import salon.billing.infrastructure.out.mock.InMemorySettlementRepository;
import salon.billing.infrastructure.out.mock.InProcessEventPublisherAdapter;
import salon.billing.infrastructure.out.mock.NotificationMockAdapter;
import salon.billing.infrastructure.out.mock.PdfGeneratorMockAdapter;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy akceptacyjne kontekstu Fakturowania i Rozliczeń — pełne przypadki użycia end-to-end
 * sterowane adapterami wejściowymi. Kontekst złożony przez Springa (wstrzykiwanie zależności),
 * z prawdziwą persystencją w pamięci, generatorem PDF oraz realnym InProcessEventPublisherAdapter
 * (z kodu źródłowego) zbierającym wyemitowane zdarzenia.
 *
 * Każda metoda dostaje świeży kontekst (@DirtiesContext), aby stan i lista zdarzeń się nie nakładały.
 */
@SpringBootTest(classes = BillingContextAcceptanceTest.BillingTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BillingContextAcceptanceTest {

    @Autowired private SettlementEventListener settlementListener;
    @Autowired private BillingEventSubscriberAdapter billingSubscriber;
    @Autowired private ProcessPayment paymentService;
    @Autowired private SettlementDatabaseRepository settlementRepository;
    @Autowired private DocumentDatabaseRepository documentRepository;
    @Autowired private InProcessEventPublisherAdapter eventPublisher;

    private boolean hasEvent(Class<? extends DomainEvent> type) {
        return eventPublisher.publishedEvents().stream().anyMatch(type::isInstance);
    }

    /** Inicjalizuje otwartą należność dla zamówienia (zdarzenie OrderReadyForSettlement). */
    private void openSettlement(String orderId, long total) {
        settlementListener.on(new OrderReadyForSettlementEvent(
                UUID.randomUUID(), orderId, new BigDecimal(total), "PLN", Instant.now()));
    }

    // ===================================================================================
    // UC-FIR-03: Procesowanie płatności
    // ===================================================================================
    @Test
    void uc03_shouldRegisterFullPaymentAndSettle() {
        openSettlement("ORD-1", 100000);

        // Księgowy księguje pełną wpłatę
        paymentService.processPayment(new ProcessPaymentCommand("ORD-1", "TX-1", new BigDecimal("100000"), "PLN"));

        // Saldo domknięte, emisja PaymentRegistered + SettlementCompleted
        assertThat(settlementRepository.findByOrderId(new OrderId("ORD-1")).orElseThrow().status())
                .isEqualTo(SettlementStatus.SETTLED);
        assertThat(hasEvent(PaymentRegisteredEvent.class)).isTrue();
        assertThat(hasEvent(SettlementCompletedEvent.class)).isTrue();
    }

    @Test
    void uc03_a1_shouldRegisterPartialPayment() {
        openSettlement("ORD-2", 100000);

        // Kwota różna od wymaganej — księgowanie częściowe
        paymentService.processPayment(new ProcessPaymentCommand("ORD-2", "TX-1", new BigDecimal("40000"), "PLN"));

        assertThat(settlementRepository.findByOrderId(new OrderId("ORD-2")).orElseThrow().status())
                .isEqualTo(SettlementStatus.PARTIAL_PAYMENT);
    }

    // ===================================================================================
    // UC-FIR-01: Wysłanie prośby o zadatek
    // ===================================================================================
    @Test
    void uc01_shouldGenerateAdvanceRequest() {
        openSettlement("ORD-3", 100000);

        // Brak pojazdu na placu — kontekst wystawia prośbę o zadatek
        billingSubscriber.handleVehicleIsNotOnStock(
                new BillingEventSubscriberAdapter.VehicleIsNotOnStock("ORD-3"));

        // Dokument przypisany do zamówienia, należność oznaczona jako zadatek żądany, emisja AdvancePaymentRequested
        assertThat(documentRepository.findByOrderId(new OrderId("ORD-3"))).isNotEmpty();
        assertThat(settlementRepository.findByOrderId(new OrderId("ORD-3")).orElseThrow().isAdvanceRequested())
                .isTrue();
        assertThat(hasEvent(AdvancePaymentRequestedEvent.class)).isTrue();
    }

    // ===================================================================================
    // UC-FIR-02: Stworzenie faktury końcowej
    // ===================================================================================
    @Test
    void uc02_shouldGenerateFinalInvoice() {
        openSettlement("ORD-4", 100000);

        // Rezerwacja pojazdu z placu — kontekst wystawia fakturę końcową
        billingSubscriber.handleVehicleReservedFromStock(
                new BillingEventSubscriberAdapter.VehicleReservedFromStock("ORD-4", "VIN-4"));

        // Faktura PDF utworzona i przypisana do zamówienia, emisja InvoiceCreated
        assertThat(documentRepository.findByOrderId(new OrderId("ORD-4"))).isNotEmpty();
        assertThat(hasEvent(InvoiceCreatedEvent.class)).isTrue();
    }

    // ===================================================================================
    // Złożenie kontekstu (wstrzykiwanie zależności)
    // ===================================================================================
    @Configuration
    static class BillingTestConfig {

        @Bean
        InProcessEventPublisherAdapter eventPublisher() {
            return new InProcessEventPublisherAdapter();
        }

        @Bean
        SettlementDatabaseRepository settlementRepository() {
            return new InMemorySettlementRepository();
        }

        @Bean
        DocumentDatabaseRepository documentRepository() {
            return new InMemoryDocumentRepository();
        }

        @Bean
        SettlementFactory settlementFactory() {
            return new SettlementFactory();
        }

        @Bean
        AccountingDocumentFactory documentFactory() {
            return new AccountingDocumentFactory();
        }

        @Bean
        InvoiceCalculationService invoiceCalculationService() {
            return new InvoiceCalculationService();
        }

        @Bean
        PdfGeneration pdfGeneration() {
            return new PdfGeneratorMockAdapter();
        }

        @Bean
        NotificationGeneration notification() {
            return new NotificationMockAdapter();
        }

        @Bean
        SellerDetails seller() {
            return new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000");
        }

        /** Deterministyczna atrapa ACL do Sprzedaży — zwraca dane nabywcy dla każdego zamówienia. */
        @Bean
        SalesIntegration salesIntegration() {
            return orderId -> new BuyerDetails("Jan Kowalski", "1234563218");
        }

        @Bean
        PaymentProcessService paymentProcessService(SettlementDatabaseRepository settlementRepository,
                                                    SettlementFactory settlementFactory,
                                                    DocumentDatabaseRepository documentRepository,
                                                    NotificationGeneration notification,
                                                    EventPublisher eventPublisher) {
            return new PaymentProcessService(settlementRepository, settlementFactory,
                    documentRepository, notification, eventPublisher);
        }

        @Bean
        DocumentGenerationService documentGenerationService(SettlementDatabaseRepository settlementRepository,
                                                            DocumentDatabaseRepository documentRepository,
                                                            InvoiceCalculationService invoiceCalculation,
                                                            AccountingDocumentFactory documentFactory,
                                                            PdfGeneration pdfGeneration,
                                                            NotificationGeneration notification,
                                                            EventPublisher eventPublisher,
                                                            SalesIntegration salesIntegration,
                                                            SellerDetails seller) {
            return new DocumentGenerationService(settlementRepository, documentRepository, invoiceCalculation,
                    documentFactory, pdfGeneration, notification, eventPublisher, salesIntegration, seller);
        }

        @Bean
        BillingEventSubscriberAdapter billingSubscriber(GenerateAdvance generateAdvance,
                                                        GenerateInvoice generateInvoice) {
            return new BillingEventSubscriberAdapter(generateAdvance, generateInvoice, "FA-Księgowy");
        }

        @Bean
        SettlementEventListener settlementListener(ProcessPayment processPayment) {
            return new SettlementEventListener(processPayment);
        }
    }
}
