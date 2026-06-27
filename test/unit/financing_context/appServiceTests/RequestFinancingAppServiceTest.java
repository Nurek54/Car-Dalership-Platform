package unit.financing_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.financing.application.domain.event.FinancingApplicationFailedEvent;
import salon.financing.application.domain.model.financing.ApplicationState;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.FinancingApplicationFactory;
import salon.financing.application.port.out.BankIntegrationAcl;
import salon.financing.application.port.out.FinancingApplicationDatabaseRepository;
import salon.financing.application.port.out.SalesIntegration;
import salon.financing.application.service.ProcessFinancingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-FIN-01: Złożenie wniosku o finansowanie. */
@ExtendWith(MockitoExtension.class)
class RequestFinancingAppServiceTest {

    @Mock private FinancingApplicationDatabaseRepository applicationRepository;
    @Mock private SalesIntegration salesIntegration;
    @Mock private BankIntegrationAcl bankIntegration;
    @Mock private EventPublisher eventPublisher;

    private ProcessFinancingService service;

    @BeforeEach
    void setUp() {
        service = new ProcessFinancingService(applicationRepository, new FinancingApplicationFactory(),
                salesIntegration, bankIntegration, eventPublisher);
    }

    @Test
    void shouldSubmitApplicationToBankAndMarkPending() { // SCENARIUSZ GŁÓWNY
        // Dane kupującego i cena pochodzą z kontekstu Sprzedaży (ACL)
        when(salesIntegration.buyerDetails("ORD-1")).thenReturn(new BuyerDetails("Jan Kowalski", "1234563218"));
        when(salesIntegration.offerFinalPrice("ORD-1")).thenReturn(Money.of(100000, "PLN"));

        service.requestFinancing("ORD-1", "CUST-1");

        // Wniosek zapisany jako "W trakcie weryfikacji bankowej" (PENDING) i wysłany do banku
        ArgumentCaptor<FinancingApplication> captor = ArgumentCaptor.forClass(FinancingApplication.class);
        verify(applicationRepository).save(captor.capture());
        assertThat(captor.getValue().state()).isEqualTo(ApplicationState.PENDING);
        verify(bankIntegration).submitFinancingApplication("ORD-1");
        // Na ścieżce sukcesu nie emitujemy zdarzenia końcowego
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldEmitFailedEventWhenBuyerDataIsInvalid() { // Scenariusz alternatywny A2
        // System banku/ACL natychmiast zwraca błąd o brakujących danych (np. błędny NIP)
        when(salesIntegration.buyerDetails("ORD-2")).thenThrow(new RuntimeException("Błędny NIP"));

        service.requestFinancing("ORD-2", "CUST-2");

        // Emisja FinancingApplicationFailed, bez zapisu i bez wysyłki do banku
        verify(eventPublisher).publish(any(FinancingApplicationFailedEvent.class));
        verify(applicationRepository, never()).save(any());
        verify(bankIntegration, never()).submitFinancingApplication(any());
    }
}
