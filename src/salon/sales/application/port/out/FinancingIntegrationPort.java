package salon.sales.application.port.out;

import salon.sales.domain.model.customer.CustomerId;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

/**
 * Port wyjściowy (driven) do Kontekstu Finansowania —
 * węzeł "FinancingIntegrationPort" w docs/Architecture/SalesArchitecture.md
 * (PDF rozdz. 3.3.3: zapytania o zdolność kredytową/leasingową).
 */
public interface FinancingIntegrationPort {

    void requestFinancing(OrderId orderId, CustomerId customerId, Money amountToFinance);
}
