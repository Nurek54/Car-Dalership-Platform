package salon.billing.domain.model.payment;

import salon.billing.domain.event.AdvanceRegisteredEvent;
import salon.billing.domain.event.DepositRegisteredEvent;
import salon.shared.event.AbstractAggregateRoot;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root: pojedyncza wpłata klienta.
 * Hermetyzacja: brak setterów; kategorię ustawia WYŁĄCZNIE metoda categorizePayment().
 *
 * Zdarzenia domenowe: to agregat — na podstawie wyniku klasyfikacji — rejestruje właściwe
 * zdarzenie (zadatek -> ZadatekZaksiegowany, zaliczka -> ZaliczkaZarejestrowana).
 * Warstwa aplikacji tylko je ściąga (pullDomainEvents) i publikuje.
 */
public class Payment extends AbstractAggregateRoot {

    private final PaymentId id;
    private final OrderId orderId;
    private final Money amount;
    private PaymentCategory category; // null, dopóki wpłata nie zostanie sklasyfikowana

    public Payment(PaymentId id, OrderId orderId, Money amount) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null.");
        }
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.category = null;
    }

    /**
     * UC-ROZ-01, krok 3: nadanie kategorii biznesowej.
     * Reguła: wpłata >= wymagany zadatek -> ZADATEK; w przeciwnym razie -> ZALICZKA (A2).
     * Przy okazji agregat rejestruje odpowiednie zdarzenie domenowe.
     */
    public void categorizePayment(Money requiredDeposit) {
        if (requiredDeposit == null) {
            throw new IllegalArgumentException("requiredDeposit must not be null.");
        }
        if (this.amount.isGreaterThanOrEqualTo(requiredDeposit)) {
            this.category = PaymentCategory.DEPOSIT;
            registerEvent(new DepositRegisteredEvent(
                    UUID.randomUUID(),
                    this.id.value(),
                    this.orderId.value(),
                    Instant.now()));
        } else {
            this.category = PaymentCategory.ADVANCE;
            registerEvent(new AdvanceRegisteredEvent(
                    UUID.randomUUID(),
                    this.id.value(),
                    this.orderId.value(),
                    Instant.now()));
        }
    }

    public boolean isDeposit() {
        return this.category == PaymentCategory.DEPOSIT;
    }

    public PaymentId getId() {
        return this.id;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public Money getAmount() {
        return this.amount;
    }

    public PaymentCategory getCategory() {
        return this.category;
    }
}
