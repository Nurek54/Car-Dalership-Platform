package unit.financing_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.financing.application.domain.event.FinancingApprovedEvent;
import salon.financing.application.domain.event.FinancingRejectedEvent;
import salon.financing.application.domain.exception.FinancingApplicationNotFoundException;
import salon.financing.application.domain.model.financing.ApplicationState;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.domain.model.financing.CustomerId;
import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.FinancingApplicationFactory;
import salon.financing.application.domain.model.financing.OrderId;
import salon.financing.application.port.out.BankIntegrationAcl;
import salon.financing.application.port.out.FinancingApplicationDatabaseRepository;
import salon.financing.application.port.out.SalesIntegration;
import salon.financing.application.service.ProcessFinancingService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-FIN-02: Przetworzenie decyzji o finansowaniu. */
@ExtendWith(MockitoExtension.class)
class ProcessBankDecisionAppServiceTest {

    @Mock private FinancingApplicationDatabaseRepository applicationRepository;
    @Mock private SalesIntegration salesIntegration;
    @Mock private BankIntegrationAcl bankIntegration;
    @Mock private EventPublisher eventPublisher;

    private ProcessFinancingService service;
    private final FinancingApplicationFactory factory = new FinancingApplicationFactory();

    @BeforeEach
    void setUp() {
        service = new ProcessFinancingService(applicationRepository, factory,
                salesIntegration, bankIntegration, eventPublisher);
    }

    private FinancingApplication pendingApplication(String orderId) {
        FinancingApplication application = factory.createDraft(new OrderId(orderId), new CustomerId("CUST-1"),
                new BuyerDetails("Jan Kowalski", "1234563218"), Money.of(100000, "PLN"));
        application.submitApplication(); // PENDING
        return application;
    }

    @Test
    void shouldApproveAndEmitApprovedEvent() { // SCENARIUSZ GŁÓWNY
        FinancingApplication application = pendingApplication("ORD-1");
        when(applicationRepository.findByOrderId(new OrderId("ORD-1"))).thenReturn(Optional.of(application));

        service.processBankDecision("ORD-1", true);

        // Wniosek zatwierdzony, emisja FinancingApproved
        assertThat(application.state()).isEqualTo(ApplicationState.APPROVED);
        verify(applicationRepository).save(application);
        verify(eventPublisher).publish(any(FinancingApprovedEvent.class));
    }

    @Test
    void shouldRejectAndEmitRejectedEvent() { // Scenariusz alternatywny A1
        FinancingApplication application = pendingApplication("ORD-2");
        when(applicationRepository.findByOrderId(new OrderId("ORD-2"))).thenReturn(Optional.of(application));

        service.processBankDecision("ORD-2", false);

        // Wniosek odrzucony, emisja FinancingRejected
        assertThat(application.state()).isEqualTo(ApplicationState.REJECTED);
        verify(applicationRepository).save(application);
        verify(eventPublisher).publish(any(FinancingRejectedEvent.class));
    }

    @Test
    void shouldThrowWhenApplicationNotFound() {
        // Brak wniosku dla wskazanego zamówienia
        when(applicationRepository.findByOrderId(new OrderId("ORD-NONE"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processBankDecision("ORD-NONE", true))
                .isInstanceOf(FinancingApplicationNotFoundException.class);
    }
}
