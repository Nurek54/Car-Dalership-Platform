package salon.financing.domain.model.financing;

import salon.financing.domain.event.FinancingApprovedEvent;
import salon.financing.domain.event.FinancingRejectedEvent;
import salon.shared.event.AbstractAggregateRoot;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root: wniosek finansowy (UC-FIN-01).
 *
 * Asynchroniczność decyzji: wysłanie wniosku ustawia SUBMITTED_TO_BANK. Agregat NIE pozwala
 * ręcznie ustawić APPROVED — wymaga zweryfikowanej decyzji (FinancingDecision) z warstwy ACL.
 */
public class FinancingApplication extends AbstractAggregateRoot {

    private final ApplicationId id;
    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money requestedAmount;
    private ApplicationState state;
    private FinancingDecision bankDecision; // null, dopóki bank nie odpowie

    public FinancingApplication(ApplicationId id,
                                OrderId orderId,
                                CustomerId customerId,
                                Money requestedAmount) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null.");
        }
        if (requestedAmount == null) {
            throw new IllegalArgumentException("requestedAmount must not be null.");
        }
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.requestedAmount = requestedAmount;
        this.state = ApplicationState.DRAFT;
        this.bankDecision = null;
    }

    // UC-FIN-01: wysłanie wniosku do banku.
    public void submitApplication() {
        if (this.state != ApplicationState.DRAFT) {
            throw new IllegalStateException("Only a DRAFT application can be submitted, was: " + this.state);
        }
        this.state = ApplicationState.SUBMITTED_TO_BANK;
    }

    // UC-FIN-01: przetworzenie decyzji banku (przez ACL). Zależnie od statusu -> APPROVED/REJECTED.
    public void processBankDecision(FinancingDecision decision) {
        if (decision == null) {
            throw new IllegalArgumentException("decision must not be null.");
        }
        if (this.state != ApplicationState.SUBMITTED_TO_BANK) {
            throw new IllegalStateException(
                    "Decision can only be processed for a submitted application, was: " + this.state);
        }
        this.bankDecision = decision;
        if (decision.status() == DecisionStatus.APPROVED) {
            this.state = ApplicationState.APPROVED;
            registerEvent(new FinancingApprovedEvent(UUID.randomUUID(), this.orderId.value(), Instant.now()));
        } else {
            this.state = ApplicationState.REJECTED;
            registerEvent(new FinancingRejectedEvent(UUID.randomUUID(), this.orderId.value(), Instant.now()));
        }
    }

    public ApplicationId getId() {
        return this.id;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public CustomerId getCustomerId() {
        return this.customerId;
    }

    public Money getRequestedAmount() {
        return this.requestedAmount;
    }

    public ApplicationState getState() {
        return this.state;
    }

    public FinancingDecision getBankDecision() {
        return this.bankDecision;
    }
}
