package salon.financing.application.domain.model.financing;

import salon.common.model.Money;

/**
 * FACTORY (Figure 42) — creates a valid {@link FinancingApplication} aggregate in the DRAFT state,
 * assigning it a new application identifier ({@link ApplicationId}). The buyer data and the financing amount
 * (the offer's final price) are fetched by the application layer from the Sales Context (SalesIntegration)
 * and passed here as Financing-local types. An atomic operation — it never returns an inconsistent object.
 */
public class FinancingApplicationFactory {

    public FinancingApplication createDraft(OrderId orderId,
                                            CustomerId customerId,
                                            BuyerDetails buyerDetails,
                                            Money moneyForFunding) {
        return new FinancingApplication(ApplicationId.generate(), orderId, customerId,
                buyerDetails, moneyForFunding, ApplicationState.DRAFT);
    }
}
