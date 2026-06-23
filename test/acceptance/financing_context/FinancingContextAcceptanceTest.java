package acceptance.financing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;
import salon.common.model.Money;
import salon.financing.application.domain.event.FinancingApplicationFailedEvent;
import salon.financing.application.domain.event.FinancingApprovedEvent;
import salon.financing.application.domain.event.FinancingRejectedEvent;
import salon.financing.application.domain.model.financing.ApplicationState;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.domain.model.financing.FinancingApplicationFactory;
import salon.financing.application.domain.model.financing.OrderId;
import salon.financing.application.port.in.ProcessFinancing;
import salon.financing.application.port.out.BankIntegrationAcl;
import salon.financing.application.port.out.FinancingApplicationDatabaseRepository;
import salon.financing.application.port.out.SalesIntegration;
import salon.financing.application.service.ProcessFinancingService;
import salon.financing.infrastructure.in.messaging.FinancingEventListener;
import salon.financing.infrastructure.out.mock.BankIntegrationMockAdapter;
import salon.financing.infrastructure.out.mock.InMemoryFinancingRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy akceptacyjne kontekstu Finansowania — pełne przypadki użycia end-to-end sterowane
 * adapterem nasłuchującym. Kontekst złożony przez Springa (wstrzykiwanie zależności), z prawdziwą
 * persystencją w pamięci, atrapą banku oraz nasłuchującym EventPublisherem zbierającym zdarzenia.
 *
 * Każda metoda dostaje świeży kontekst (@DirtiesContext), aby stan i lista zdarzeń się nie nakładały.
 */
@SpringBootTest(classes = FinancingContextAcceptanceTest.FinancingTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FinancingContextAcceptanceTest {

    @Autowired private FinancingEventListener listener;
    @Autowired private FinancingApplicationDatabaseRepository repository;
    @Autowired private RecordingEventPublisher eventPublisher;

    // ===================================================================================
    // UC-FIN-01: Złożenie wniosku o finansowanie
    // ===================================================================================
    @Test
    void uc01_shouldSubmitFinancingApplication() {
        // Klient w CRM wybrał finansowanie — przychodzi zdarzenie FinancingRequested
        listener.handleFinancingRequested(new FinancingEventListener.FinancingRequested("ORD-1", "CUST-1"));

        // Wniosek zapisany lokalnie jako "W trakcie weryfikacji bankowej" (PENDING)
        assertThat(repository.findByOrderId(new OrderId("ORD-1")).orElseThrow().state())
                .isEqualTo(ApplicationState.PENDING);
    }

    @Test
    void uc01_a2_shouldEmitFailedWhenBuyerDataInvalid() {
        // Dla zamówienia ORD-BAD ACL Sprzedaży zwraca błąd danych (np. błędny NIP)
        listener.handleFinancingRequested(new FinancingEventListener.FinancingRequested("ORD-BAD", "CUST-9"));

        // Emisja FinancingApplicationFailed, brak zapisanego wniosku
        assertThat(eventPublisher.has(FinancingApplicationFailedEvent.class)).isTrue();
        assertThat(repository.findByOrderId(new OrderId("ORD-BAD"))).isEmpty();
    }

    // ===================================================================================
    // UC-FIN-02: Przetworzenie decyzji o finansowaniu
    // ===================================================================================
    @Test
    void uc02_shouldApproveFinancing() {
        // Najpierw składamy wniosek (stan PENDING)
        listener.handleFinancingRequested(new FinancingEventListener.FinancingRequested("ORD-2", "CUST-2"));

        // Bank przekazuje pozytywną decyzję
        listener.handleFinancingDecisionReceived(
                new FinancingEventListener.FinancingDecisionReceivedFromBank("ORD-2", true));

        // Wniosek zatwierdzony, emisja FinancingApproved
        assertThat(repository.findByOrderId(new OrderId("ORD-2")).orElseThrow().state())
                .isEqualTo(ApplicationState.APPROVED);
        assertThat(eventPublisher.has(FinancingApprovedEvent.class)).isTrue();
    }

    @Test
    void uc02_a1_shouldRejectFinancing() {
        listener.handleFinancingRequested(new FinancingEventListener.FinancingRequested("ORD-3", "CUST-3"));

        // Bank przekazuje decyzję odmowną
        listener.handleFinancingDecisionReceived(
                new FinancingEventListener.FinancingDecisionReceivedFromBank("ORD-3", false));

        // Wniosek odrzucony, emisja FinancingRejected
        assertThat(repository.findByOrderId(new OrderId("ORD-3")).orElseThrow().state())
                .isEqualTo(ApplicationState.REJECTED);
        assertThat(eventPublisher.has(FinancingRejectedEvent.class)).isTrue();
    }

    // ===================================================================================
    // Złożenie kontekstu (wstrzykiwanie zależności)
    // ===================================================================================
    @Configuration
    static class FinancingTestConfig {

        @Bean
        RecordingEventPublisher eventPublisher() {
            return new RecordingEventPublisher();
        }

        @Bean
        FinancingApplicationDatabaseRepository applicationRepository() {
            return new InMemoryFinancingRepository();
        }

        @Bean
        FinancingApplicationFactory applicationFactory() {
            return new FinancingApplicationFactory();
        }

        /** Deterministyczna atrapa ACL do Sprzedaży — dla ORD-BAD zwraca błąd danych kupującego. */
        @Bean
        SalesIntegration salesIntegration() {
            return new SalesIntegration() {
                @Override
                public BuyerDetails buyerDetails(String orderId) {
                    if ("ORD-BAD".equals(orderId)) {
                        throw new RuntimeException("Błędny NIP");
                    }
                    return new BuyerDetails("Jan Kowalski", "1234563218");
                }

                @Override
                public Money offerFinalPrice(String orderId) {
                    return Money.of(100000, "PLN");
                }
            };
        }

        @Bean
        BankIntegrationAcl bankIntegration() {
            return new BankIntegrationMockAdapter();
        }

        @Bean
        ProcessFinancingService processFinancingService(FinancingApplicationDatabaseRepository repository,
                                                        FinancingApplicationFactory factory,
                                                        SalesIntegration salesIntegration,
                                                        BankIntegrationAcl bankIntegration,
                                                        EventPublisher eventPublisher) {
            return new ProcessFinancingService(repository, factory, salesIntegration, bankIntegration, eventPublisher);
        }

        @Bean
        FinancingEventListener financingEventListener(ProcessFinancing processFinancing) {
            return new FinancingEventListener(processFinancing);
        }
    }

    /** Nasłuchujący adapter zdarzeń — zbiera wyemitowane zdarzenia dziedzinowe na potrzeby asercji. */
    static final class RecordingEventPublisher implements EventPublisher {
        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }

        boolean has(Class<? extends DomainEvent> type) {
            return events.stream().anyMatch(type::isInstance);
        }
    }
}
