package salon.financing.application.domain.model.financing;

import salon.common.model.Money;
import salon.financing.application.domain.exception.IllegalApplicationStateException;

/**
 * AGGREGATE ROOT (Figure 43) – Financing Application.
 *
 * The aggregate guards the application's state machine (DRAFT -> PENDING -> APPROVED/REJECTED). The dealership never
 * makes the credit decision itself — the aggregate only reflects the status assigned
 * by the external bank system (the sole decision-maker). References to the order and the customer are
 * disjoint (through identifiers).
 */
public class FinancingApplication {

    private final ApplicationId applicationId;
    private final OrderId orderId;
    private final CustomerId customerId;
    private final BuyerDetails buyerDetails;
    private final Money moneyForFunding;
    private ApplicationState state;

    /** Package-private constructor — instances are created only by {@link FinancingApplicationFactory}. */
    FinancingApplication(ApplicationId applicationId, OrderId orderId, CustomerId customerId,
                         BuyerDetails buyerDetails, Money moneyForFunding, ApplicationState state) {
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
        if (state == null) {
            throw new IllegalArgumentException("state must not be null.");
        }
        this.applicationId = applicationId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.buyerDetails = buyerDetails;
        this.moneyForFunding = moneyForFunding;
        this.state = state;
    }

    /** UC-FIN-01: sending the application to the bank — transition to the "Under bank verification" state. */
    public void submitApplication() {
        if (this.state != ApplicationState.DRAFT) {
            throw new IllegalApplicationStateException(
                    "Only an application in the DRAFT state can be sent (current: " + this.state + ").");
        }
        this.state = ApplicationState.PENDING;
    }

    /** UC-FIN-02: registering the bank's positive decision. */
    public void approve() {
        if (this.state != ApplicationState.PENDING) {
            throw new IllegalApplicationStateException(
                    "Only an application in the PENDING state can be approved (current: " + this.state + ").");
        }
        this.state = ApplicationState.APPROVED;
    }

    /** UC-FIN-02 / A1: registering the bank's negative decision. */
    public void reject() {
        if (this.state != ApplicationState.PENDING) {
            throw new IllegalApplicationStateException(
                    "Only an application in the PENDING state can be rejected (current: " + this.state + ").");
        }
        this.state = ApplicationState.REJECTED;
    }

    public ApplicationId applicationId() {
        return applicationId;
    }

    public OrderId orderId() {
        return orderId;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public BuyerDetails buyerDetails() {
        return buyerDetails;
    }

    public Money moneyForFunding() {
        return moneyForFunding;
    }

    public ApplicationState state() {
        return state;
    }
}
