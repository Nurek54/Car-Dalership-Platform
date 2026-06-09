package salon.financing.domain.model.financing;

import salon.shared.model.OrderId;

/**
 * Fabryka (FinancingArchitecture.md): powołuje poprawny agregat FinancingApplication
 * w stanie DRAFT, nadając mu nowy identyfikator wniosku.
 */
public class FinancingApplicationFactory {

    public FinancingApplication createFor(OrderId orderId, CustomerId customerId) {
        return new FinancingApplication(ApplicationId.generate(), orderId, customerId);
    }
}
