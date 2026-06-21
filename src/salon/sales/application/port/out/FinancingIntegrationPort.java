package salon.sales.application.port.out;

import salon.sales.application.domain.model.customer.CustomerId;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * Outbound port (driven) to the Financing Context —
 * the "FinancingIntegrationPort" node in docs/Architecture/SalesArchitecture.md
 * (PDF chapter 3.3.3: queries about credit/leasing capacity).
 */
public interface FinancingIntegrationPort {

    void requestFinancing(OrderId orderId, CustomerId customerId, Money amountToFinance);
}
