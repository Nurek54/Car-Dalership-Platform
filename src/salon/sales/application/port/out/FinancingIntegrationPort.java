package salon.sales.application.port.out;

import salon.sales.domain.model.customer.CustomerId;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

/**
 * Port wyjściowy (driven) integracji z Kontekstem Finansowania —
 * węzeł "FinancingIntegrationPort" w docs/Architecture/SalesArchitecture.md.
 *
 * Gdy klient wybiera finansowanie jako formę płatności, Sprzedaż inicjuje
 * wniosek kredytowy w kontekście Finansowania. Konkretną komunikację realizuje adapter.
 */
public interface FinancingIntegrationPort {

    /**
     * Inicjuje wniosek o finansowanie dla danego zamówienia/klienta na wskazaną kwotę.
     */
    void requestFinancing(OrderId orderId, CustomerId customerId, Money amountToFinance);
}
