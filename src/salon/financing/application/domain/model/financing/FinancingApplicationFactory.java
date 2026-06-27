package salon.financing.application.domain.model.financing;

import salon.common.model.Money;

public class FinancingApplicationFactory {

    public FinancingApplication createDraft(OrderId orderId,
                                            CustomerId customerId,
                                            BuyerDetails buyerDetails,
                                            Money moneyForFunding) {
        return new FinancingApplication(ApplicationId.generate(), orderId, customerId,
                buyerDetails, moneyForFunding, ApplicationState.DRAFT);
    }
}
