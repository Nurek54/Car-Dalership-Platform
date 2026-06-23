package unit.billing_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.billing.application.domain.event.AdvancePaymentRegisteredEvent;
import salon.billing.application.domain.event.AdvancePaymentRequestedEvent;
import salon.billing.application.domain.event.PaymentRegisteredEvent;
import salon.billing.application.domain.event.SettlementCompletedEvent;
import salon.billing.application.domain.exception.IllegalSettlementStateException;
import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.model.settlement.SettlementStatus;
import salon.common.model.Money;
import salon.common.model.OrderId;

import static org.assertj.core.api.Assertions.*;

/** UC-FIR-03: Agregat Settlement (rozliczenie salda zamówienia). */
class SettlementTest {

    private final SettlementFactory factory = new SettlementFactory();

    private Settlement settlement(String orderId, long total) {
        return factory.createNew(new OrderId(orderId), Money.of(total, "PLN"));
    }

    @Test
    void shouldBePartiallyPaidAfterUnderpayment() { // Scenariusz alternatywny A1
        Settlement settlement = settlement("ORD-1", 100000);

        // Kwota wpłaty mniejsza niż wymagana — księgowanie częściowe
        settlement.registerPayment("TX-1", Money.of(40000, "PLN"));

        assertThat(settlement.status()).isEqualTo(SettlementStatus.PARTIAL_PAYMENT);
        assertThat(settlement.outstandingBalance()).isEqualTo(Money.of(60000, "PLN"));
        assertThat(settlement.getDomainEvents()).hasAtLeastOneElementOfType(PaymentRegisteredEvent.class);
    }

    @Test
    void shouldBeSettledWhenFullyPaid() { // SCENARIUSZ GŁÓWNY
        Settlement settlement = settlement("ORD-2", 100000);

        // Pełna wpłata domyka saldo
        settlement.registerPayment("TX-1", Money.of(100000, "PLN"));

        assertThat(settlement.status()).isEqualTo(SettlementStatus.SETTLED);
        // Kontekst emituje PaymentRegistered i SettlementCompleted
        assertThat(settlement.getDomainEvents())
                .hasAtLeastOneElementOfType(PaymentRegisteredEvent.class)
                .hasAtLeastOneElementOfType(SettlementCompletedEvent.class);
    }

    @Test
    void shouldEmitAdvanceRegisteredOnFirstPaymentWhenRequested() {
        Settlement settlement = settlement("ORD-3", 100000);

        // UC-FIR-01: zażądano zadatku, a następnie klient wpłacił pierwszą ratę
        settlement.requestAdvancePayment();
        assertThat(settlement.isAdvanceRequested()).isTrue();
        assertThat(settlement.getDomainEvents()).hasAtLeastOneElementOfType(AdvancePaymentRequestedEvent.class);

        settlement.registerPayment("TX-1", Money.of(10000, "PLN"));
        assertThat(settlement.getDomainEvents()).hasAtLeastOneElementOfType(AdvancePaymentRegisteredEvent.class);
    }

    @Test
    void shouldRejectPaymentWhenAlreadySettled() {
        Settlement settlement = settlement("ORD-4", 100000);
        settlement.registerPayment("TX-1", Money.of(100000, "PLN")); // SETTLED

        // Do rozliczonego salda nie można już księgować wpłat
        assertThatThrownBy(() -> settlement.registerPayment("TX-2", Money.of(1, "PLN")))
                .isInstanceOf(IllegalSettlementStateException.class);
    }
}
