package salon.billing.application.domain.model.settlement;

import salon.billing.application.domain.event.AdvancePaymentRegisteredEvent;
import salon.billing.application.domain.event.AdvancePaymentRequestedEvent;
import salon.billing.application.domain.event.PaymentRegisteredEvent;
import salon.billing.application.domain.event.SettlementCompletedEvent;
import salon.common.event.AbstractAggregateRoot;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root: rozliczenie zamówienia (UC-FIR-01 oraz UC-FIR-03).
 *
 * Centralny mechanizm kontroli finansowej kontraktu, powiązany z nim przez niemutowalny OrderId.
 * Agregat jest WYŁĄCZNYM właścicielem reguł matematycznych:
 *  - wpłaty ({@link Payment}) to encje lokalne — nie mają własnego repozytorium,
 *  - rejestracja wpłaty (registerPayment) dokłada ją do kolekcji i natychmiast, w sposób ukryty,
 *    przelicza saldo (recalculateBalance),
 *  - na podstawie bilansu agregat SAM zmienia status (OPEN -> PARTIAL_PAYMENT -> SETTLED),
 *  - osiągnięcie zerowego zadłużenia powoduje wewnętrzną kreację SettlementCompletedEvent.
 */
public class Settlement extends AbstractAggregateRoot {

    private final SettlementId id;
    private final OrderId orderId;
    private final Money totalAmount;          // wartość kontraktu do rozliczenia
    private final List<Payment> payments;
    private SettlementStatus status;
    private boolean advanceRequested;   // UC-FIR-01: czy poproszono klienta o zadatek

    public Settlement(SettlementId id, OrderId orderId, Money totalAmount) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("totalAmount must not be null.");
        }
        this.id = id;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.payments = new ArrayList<>();
        this.status = SettlementStatus.OPEN;
        this.advanceRequested = false;
    }

    /**
     * UC-FIR-01: zażądanie wpłaty zadatku. Agregat rejestruje zdarzenie domenowe,
     * dzięki któremu reszta systemu (np. Sprzedaż) może zareagować asynchronicznie.
     * Zapamiętany fakt prośby pozwala rozpoznać pierwszą wpłatę jako zadatek.
     */
    public void requestAdvancePayment() {
        this.advanceRequested = true;
        registerEvent(new AdvancePaymentRequestedEvent(
                UUID.randomUUID(), this.id.value(), this.orderId.value(), Instant.now()));
    }

    /**
     * UC-FIR-03: rejestracja pojedynczej wpłaty z wyciągu bankowego.
     * Dodaje encję lokalną do kolekcji i wykonuje ukryte przeliczenie salda.
     * Krok 4: agregat ogłasza PaymentRegistered; pierwsza wpłata po prośbie o zadatek
     * dodatkowo ogłasza AdvancePaymentRegistered (trigger UC-INW-02 — zlecenie produkcji).
     */
    public void registerPayment(String transactionId, Money amount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null.");
        }
        if (!amount.currency().equals(this.totalAmount.currency())) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + amount.currency() + " and " + this.totalAmount.currency());
        }
        boolean firstPayment = this.payments.isEmpty();
        this.payments.add(new Payment(transactionId, amount, LocalDateTime.now()));

        registerEvent(new PaymentRegisteredEvent(
                UUID.randomUUID(), this.id.value(), this.orderId.value(),
                amount.amount(), amount.currency(), Instant.now()));
        if (firstPayment && this.advanceRequested) {
            registerEvent(new AdvancePaymentRegisteredEvent(
                    UUID.randomUUID(), this.id.value(), this.orderId.value(), Instant.now()));
        }
        recalculateBalance();
    }

    /**
     * Ukryta reguła matematyczna: sumuje wpłaty i autonomicznie ustala status.
     * Pokrycie całości należności emituje (jednorazowo) SettlementCompletedEvent.
     */
    private void recalculateBalance() {
        Money paid = totalPaid();

        if (paid.isGreaterThanOrEqualTo(this.totalAmount)) {
            if (this.status != SettlementStatus.SETTLED) {
                this.status = SettlementStatus.SETTLED;
                registerEvent(new SettlementCompletedEvent(
                        UUID.randomUUID(), this.id.value(), this.orderId.value(), Instant.now()));
            }
        } else if (paid.amount().signum() > 0) {
            this.status = SettlementStatus.PARTIAL_PAYMENT;
        } else {
            this.status = SettlementStatus.OPEN;
        }
    }

    private Money totalPaid() {
        Money total = new Money(BigDecimal.ZERO, this.totalAmount.currency());
        for (int i = 0; i < this.payments.size(); i++) {
            total = total.add(this.payments.get(i).getAmount());
        }
        return total;
    }

    public Money getOutstandingBalance() {
        return this.totalAmount.subtract(totalPaid());
    }

    public Money getTotalPaid() {
        return totalPaid();
    }

    public SettlementId getId() {
        return this.id;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public Money getTotalAmount() {
        return this.totalAmount;
    }

    public List<Payment> getPayments() {
        return Collections.unmodifiableList(new ArrayList<>(this.payments));
    }

    public SettlementStatus getStatus() {
        return this.status;
    }
}
