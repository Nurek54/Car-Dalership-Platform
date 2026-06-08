package salon.financing.domain.model.insurance;

import salon.financing.domain.event.PolicyActivatedEvent;
import salon.shared.event.AbstractAggregateRoot;
import salon.shared.model.Money;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root: polisa ubezpieczeniowa (UC-FIN-02).
 *
 * Ochrona wartości ubezpieczenia: polisy (szczególnie GAP) nie da się aktywować, dopóki nie
 * przypisano precyzyjnie wyliczonej wartości rezydualnej (insuredValue).
 */
public class InsurancePolicy extends AbstractAggregateRoot {

    private final PolicyId id;
    private final VinNumber vin;
    private final PolicyType type;
    private Money insuredValue; // null, dopóki nie wyliczono wartości rezydualnej
    private PolicyState state;

    public InsurancePolicy(PolicyId id, VinNumber vin, PolicyType type) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null.");
        }
        this.id = id;
        this.vin = vin;
        this.type = type;
        this.insuredValue = null;
        this.state = PolicyState.CALCULATING;
    }

    // UC-FIN-02: przypisanie wyliczonej (przez serwis dziedziny) wartości rezydualnej.
    public void assignResidualValue(Money residualValue) {
        if (residualValue == null) {
            throw new IllegalArgumentException("residualValue must not be null.");
        }
        if (residualValue.isNegative()) {
            throw new IllegalArgumentException("residualValue must not be negative.");
        }
        if (this.state != PolicyState.CALCULATING) {
            throw new IllegalStateException(
                    "Residual value can only be assigned while CALCULATING, was: " + this.state);
        }
        this.insuredValue = residualValue;
        this.state = PolicyState.PENDING_ACTIVATION;
    }

    // UC-FIN-02: aktywacja polisy po stronie ubezpieczyciela.
    public void activatePolicy(String insurerReference) {
        if (insurerReference == null || insurerReference.isBlank()) {
            throw new IllegalArgumentException("insurerReference must not be blank.");
        }
        // Najpierw chronimy wartość ubezpieczenia: bez wyliczonej wartości rezydualnej (np. dla GAP)
        // polisy nie wolno aktywować — to kluczowy niezmiennik kontekstu (UC-FIN-02).
        if (this.insuredValue == null) {
            throw new IllegalStateException("Cannot activate policy without assigned residual value.");
        }
        if (this.state != PolicyState.PENDING_ACTIVATION) {
            throw new IllegalStateException(
                    "Only a PENDING_ACTIVATION policy can be activated, was: " + this.state);
        }
        this.state = PolicyState.ACTIVE;
        registerEvent(new PolicyActivatedEvent(UUID.randomUUID(), this.vin.value(), Instant.now()));
    }

    public PolicyId getId() {
        return this.id;
    }

    public VinNumber getVin() {
        return this.vin;
    }

    public PolicyType getType() {
        return this.type;
    }

    public Money getInsuredValue() {
        return this.insuredValue;
    }

    public PolicyState getState() {
        return this.state;
    }
}
