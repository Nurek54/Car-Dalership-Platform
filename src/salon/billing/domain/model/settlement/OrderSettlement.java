package salon.billing.domain.model.settlement;

import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;

/**
 * Aggregate Root: rozliczenie końcowe zamówienia przed wydaniem pojazdu.
 *
 * Budowane krok po kroku: tworzymy z wartością pojazdu, a potem dokładamy potrącenia
 * (zadatki, finansowanie, odkup) metodami biznesowymi. Saldo i stan powstają dopiero po obliczeniu.
 */
public class OrderSettlement {

    private final SettlementId id;
    private final OrderId orderId;
    private final Money vehicleValue;

    private Money totalDeposits;
    private Money financingAmount;
    private Money tradeInValue;

    private Money finalBalance; // null, dopóki nie policzymy salda
    private SettlementState state;

    public OrderSettlement(SettlementId id, OrderId orderId, Money vehicleValue) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (vehicleValue == null) {
            throw new IllegalArgumentException("vehicleValue must not be null.");
        }
        this.id = id;
        this.orderId = orderId;
        this.vehicleValue = vehicleValue;
        // Potrącenia startują od zera w walucie pojazdu, by add() nie mieszał walut.
        Money zero = new Money(BigDecimal.ZERO, vehicleValue.currency());
        this.totalDeposits = zero;
        this.financingAmount = zero;
        this.tradeInValue = zero;
        this.finalBalance = null;
        this.state = SettlementState.OPEN;
    }

    public void applyDeposit(Money deposit) {
        if (deposit == null) {
            throw new IllegalArgumentException("deposit must not be null.");
        }
        this.totalDeposits = this.totalDeposits.add(deposit);
    }

    public void applyFinancing(Money financing) {
        if (financing == null) {
            throw new IllegalArgumentException("financing must not be null.");
        }
        this.financingAmount = this.financingAmount.add(financing);
    }

    public void applyTradeIn(Money tradeIn) {
        if (tradeIn == null) {
            throw new IllegalArgumentException("tradeIn must not be null.");
        }
        this.tradeInValue = this.tradeInValue.add(tradeIn);
    }

    /**
     * UC-ROZ-03, krok 6:
     * Saldo końcowe = Wartość pojazdu - Zadatki - Finansowanie - Wartość odkupu
     */
    public void calculateBalance() {
        Money result = this.vehicleValue;
        result = result.subtract(this.totalDeposits);
        result = result.subtract(this.financingAmount);
        result = result.subtract(this.tradeInValue);
        this.finalBalance = result;
    }

    // UC-ROZ-03, A1: saldo ujemne = nadpłata -> wymaga korekty księgowej.
    public void checkForOverpayment() {
        if (this.finalBalance == null) {
            throw new IllegalStateException("Calculate the balance first (calculateBalance).");
        }
        if (this.finalBalance.isNegative()) {
            this.state = SettlementState.REQUIRES_CORRECTION;
        }
    }

    public boolean requiresCorrection() {
        return this.state == SettlementState.REQUIRES_CORRECTION;
    }

    public void markAsSettled() {
        if (this.finalBalance == null) {
            throw new IllegalStateException("Calculate the balance first (calculateBalance).");
        }
        if (this.state == SettlementState.REQUIRES_CORRECTION) {
            throw new IllegalStateException("Settlement requires correction — it cannot be closed.");
        }
        this.state = SettlementState.SETTLED;
    }

    public SettlementId getId() {
        return this.id;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public Money getVehicleValue() {
        return this.vehicleValue;
    }

    public Money getTotalDeposits() {
        return this.totalDeposits;
    }

    public Money getFinancingAmount() {
        return this.financingAmount;
    }

    public Money getTradeInValue() {
        return this.tradeInValue;
    }

    public Money getFinalBalance() {
        return this.finalBalance;
    }

    public SettlementState getState() {
        return this.state;
    }
}
