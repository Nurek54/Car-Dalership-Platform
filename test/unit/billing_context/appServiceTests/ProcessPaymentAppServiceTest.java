package unit.billing_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.billing.application.command.ProcessPaymentCommand;
import salon.billing.application.domain.exception.SettlementNotFoundException;
import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.model.settlement.SettlementStatus;
import salon.billing.application.port.out.DocumentDatabaseRepository;
import salon.billing.application.port.out.NotificationGeneration;
import salon.billing.application.port.out.SettlementDatabaseRepository;
import salon.billing.application.service.PaymentProcessService;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** UC-FIR-03: Procesowanie płatności — serwis PaymentProcessService. */
@ExtendWith(MockitoExtension.class)
class ProcessPaymentAppServiceTest {

    @Mock private SettlementDatabaseRepository settlementRepository;
    @Mock private DocumentDatabaseRepository documentRepository;
    @Mock private NotificationGeneration notification;
    @Mock private EventPublisher eventPublisher;

    private PaymentProcessService service;
    private final SettlementFactory factory = new SettlementFactory();

    @BeforeEach
    void setUp() {
        service = new PaymentProcessService(settlementRepository, factory,
                documentRepository, notification, eventPublisher);
    }

    @Test
    void shouldInitializeOpenSettlement() {
        // Inicjalizacja należności (zdarzenie OrderReadyForSettlement)
        service.initializeSettlement(new OrderId("ORD-1"), Money.of(100000, "PLN"));

        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    void shouldRegisterFullPaymentAndSettle() { // SCENARIUSZ GŁÓWNY
        Settlement settlement = factory.createNew(new OrderId("ORD-2"), Money.of(100000, "PLN"));
        when(settlementRepository.findByOrderId(new OrderId("ORD-2"))).thenReturn(Optional.of(settlement));

        service.processPayment(new ProcessPaymentCommand("ORD-2", "TX-1", new BigDecimal("100000"), "PLN"));

        // Saldo domknięte, zdarzenia opublikowane (PaymentRegistered + SettlementCompleted)
        assertThat(settlement.status()).isEqualTo(SettlementStatus.SETTLED);
        verify(settlementRepository).save(settlement);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    void shouldRegisterPartialPayment() { // Scenariusz alternatywny A1
        Settlement settlement = factory.createNew(new OrderId("ORD-3"), Money.of(100000, "PLN"));
        when(settlementRepository.findByOrderId(new OrderId("ORD-3"))).thenReturn(Optional.of(settlement));

        service.processPayment(new ProcessPaymentCommand("ORD-3", "TX-1", new BigDecimal("40000"), "PLN"));

        // Kwota różna od wymaganej — księgowanie częściowe (PARTIAL_PAYMENT)
        assertThat(settlement.status()).isEqualTo(SettlementStatus.PARTIAL_PAYMENT);
        verify(settlementRepository).save(settlement);
    }

    @Test
    void shouldThrowWhenSettlementNotFound() {
        when(settlementRepository.findByOrderId(new OrderId("ORD-NONE"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processPayment(
                new ProcessPaymentCommand("ORD-NONE", "TX-1", new BigDecimal("100"), "PLN")))
                .isInstanceOf(SettlementNotFoundException.class);
    }

    @Test
    void shouldSendRemindersForUnsettledSettlements() {
        // Cykliczne przypomnienia tylko dla nierozliczonych należności
        Settlement open = factory.createNew(new OrderId("ORD-4"), Money.of(100000, "PLN"));
        when(settlementRepository.findAll()).thenReturn(List.of(open));

        service.sendPaymentReminders();

        verify(notification).notifyPaymentReminder(eq(new OrderId("ORD-4")), any(Money.class));
    }
}
