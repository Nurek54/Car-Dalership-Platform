package salon.financing.application.domain.model.financing;

import salon.common.model.Money;
import salon.financing.application.domain.exception.IllegalApplicationStateException;

/**
 * KORZEŃ AGREGATU (Rysunek 43) – Wniosek o finansowanie.
 *
 * Agregat strzeże maszyny stanów wniosku (DRAFT -> PENDING -> APPROVED/REJECTED). Salon nigdy
 * nie podejmuje decyzji kredytowej samodzielnie — agregat jedynie odzwierciedla status nadany
 * przez zewnętrzny system bankowy (jedyny decydent). Odwołania do zamówienia i klienta są
 * rozłączne (przez identyfikatory).
 */
public class FinancingApplication {

    private final ApplicationId applicationId;
    private final OrderId orderId;
    private final CustomerId customerId;
    private final BuyerDetails buyerDetails;
    private final Money moneyForFunding;
    private ApplicationState state;

    /** Konstruktor pakietowy — egzemplarze tworzy wyłącznie {@link FinancingApplicationFactory}. */
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

    /** UC-FIN-01: wysłanie wniosku do banku — przejście w stan „W trakcie weryfikacji bankowej". */
    public void submitApplication() {
        if (this.state != ApplicationState.DRAFT) {
            throw new IllegalApplicationStateException(
                    "Wysłać można tylko wniosek w stanie DRAFT (aktualny: " + this.state + ").");
        }
        this.state = ApplicationState.PENDING;
    }

    /** UC-FIN-02: zarejestrowanie pozytywnej decyzji banku. */
    public void approve() {
        if (this.state != ApplicationState.PENDING) {
            throw new IllegalApplicationStateException(
                    "Zatwierdzić można tylko wniosek w stanie PENDING (aktualny: " + this.state + ").");
        }
        this.state = ApplicationState.APPROVED;
    }

    /** UC-FIN-02 / A1: zarejestrowanie negatywnej decyzji banku. */
    public void reject() {
        if (this.state != ApplicationState.PENDING) {
            throw new IllegalApplicationStateException(
                    "Odrzucić można tylko wniosek w stanie PENDING (aktualny: " + this.state + ").");
        }
        this.state = ApplicationState.REJECTED;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public BuyerDetails getBuyerDetails() {
        return buyerDetails;
    }

    public Money getMoneyForFunding() {
        return moneyForFunding;
    }

    public ApplicationState getState() {
        return state;
    }
}
