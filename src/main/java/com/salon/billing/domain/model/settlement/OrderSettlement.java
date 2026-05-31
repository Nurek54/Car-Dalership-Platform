package main.java.com.salon.billing.domain.model.settlement;

import main.java.com.salon.billing.domain.model.shared.Money;
import main.java.com.salon.billing.domain.model.shared.OrderId;

/**
 * Aggregate Root: rozliczenie końcowe zamówienia przed wydaniem pojazdu.
 *
 * Wszystkie kwoty wejściowe są niemutowalne. Wynik (finalBalance) i stan
 * powstają dopiero po wywołaniu metod biznesowych.
 */
public class OrderSettlement {

    private final SettlementId id;
    private final OrderId orderId;
    private final Money vehicleValue;
    private final Money totalDeposits;
    private final Money financingAmount;
    private final Money tradeInValue;

    private Money finalBalance; // null, dopóki nie policzymy salda
    private SettlementState state;

    public OrderSettlement(SettlementId id,
                           OrderId orderId,
                           Money vehicleValue,
                           Money totalDeposits,
                           Money financingAmount,
                           Money tradeInValue) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (vehicleValue == null) {
            throw new IllegalArgumentException("vehicleValue must not be null.");
        }
        if (totalDeposits == null) {
            throw new IllegalArgumentException("totalDeposits must not be null.");
        }
        if (financingAmount == null) {
            throw new IllegalArgumentException("financingAmount must not be null.");
        }
        if (tradeInValue == null) {
            throw new IllegalArgumentException("tradeInValue must not be null.");
        }

        this.id = id;
        this.orderId = orderId;
        this.vehicleValue = vehicleValue;
        this.totalDeposits = totalDeposits;
        this.financingAmount = financingAmount;
        this.tradeInValue = tradeInValue;
        this.finalBalance = null;
        this.state = SettlementState.OPEN;
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

    // Zamknięcie rozliczenia (scenariusz główny, po wygenerowaniu dokumentu).
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
