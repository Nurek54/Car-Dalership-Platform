package salon.financing.domain.model.financing;

import salon.financing.domain.event.FinancingApprovedEvent;
import salon.financing.domain.event.FinancingRejectedEvent;
import salon.shared.event.AbstractAggregateRoot;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root: wniosek finansowy (UC-FIN-01), zgodny 1:1 z diagramem agregatu.
 *
 *   pola:   applicationId, orderId, customerId, state
 *   metody: submitApplication (DRAFT -> PENDING), approve (PENDING -> APPROVED),
 *           reject (PENDING -> REJECTED)
 *
 * Decyzja banku przychodzi z warstwy aplikacji (przez ACL) i przekłada się na approve()/reject().
 */
public class FinancingApplication extends AbstractAggregateRoot {

    private final ApplicationId applicationId;
    private final OrderId orderId;
    private final CustomerId customerId;
    private final BuyerDetails buyerDetails;
    private ApplicationState state;
    private final Money moneyForFunding;

    public FinancingApplication(ApplicationId applicationId,
                                OrderId orderId,
                                CustomerId customerId,
                                BuyerDetails buyerDetails,
                                Money moneyForFunding) {
        if (applicationId == null) {
            throw new IllegalArgumentException("applicationId must not be null.");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null.");
        }
        if (buyerDetails == null) {
            throw new IllegalArgumentException("buyerDetails must not be null.");
        }
        if (moneyForFunding == null) {
            throw new IllegalArgumentException("moneyForFunding must not be null.");
        }
        this.applicationId = applicationId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.buyerDetails = buyerDetails;
        this.moneyForFunding = moneyForFunding;
        this.state = ApplicationState.DRAFT;
    }

    // UC-FIN-01: wysłanie wniosku do banku.
    public void submitApplication() {
        if (this.state != ApplicationState.DRAFT) {
            throw new IllegalStateException("Only a DRAFT application can be submitted, was: " + this.state);
        }
        this.state = ApplicationState.PENDING;
    }

    // UC-FIN-01: pozytywna decyzja banku (zweryfikowana przez ACL).
    public void approve() {
        if (this.state != ApplicationState.PENDING) {
            throw new IllegalStateException("Only a PENDING application can be approved, was: " + this.state);
        }
        this.state = ApplicationState.APPROVED;
        registerEvent(new FinancingApprovedEvent(UUID.randomUUID(), this.orderId.value(), Instant.now()));
    }

    // UC-FIN-01 A2: negatywna decyzja banku.
    public void reject() {
        if (this.state != ApplicationState.PENDING) {
            throw new IllegalStateException("Only a PENDING application can be rejected, was: " + this.state);
        }
        this.state = ApplicationState.REJECTED;
        registerEvent(new FinancingRejectedEvent(UUID.randomUUID(), this.orderId.value(), Instant.now()));
    }

    public ApplicationId getId() {
        return this.applicationId;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public CustomerId getCustomerId() {
        return this.customerId;
    }

    public ApplicationState getState() {
        return this.state;
    }

    public BuyerDetails getBuyerDetails() {
        return this.buyerDetails;
    }

    public Money getMoneyForFunding() {
        return this.moneyForFunding;
    }
}
