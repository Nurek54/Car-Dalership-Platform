package salon.financing.domain.model.financing;

import salon.shared.model.Money;
import salon.shared.model.OrderId;

/**
 * Fabryka (FinancingArchitecture.md): powołuje poprawny agregat FinancingApplication
 * w stanie DRAFT, nadając mu nowy identyfikator wniosku. Dane nabywcy i kwota
 * finansowania (cena końcowa oferty) dociągane są przez warstwę aplikacji
 * z Kontekstu Sprzedaży (CrmIntegrationPort) i przekazywane tu jako lokalne typy.
 */
public class FinancingApplicationFactory {

    public FinancingApplication createFor(OrderId orderId,
                                          CustomerId customerId,
                                          BuyerDetails buyerDetails,
                                          Money moneyForFunding) {
        return new FinancingApplication(
                ApplicationId.generate(), orderId, customerId, buyerDetails, moneyForFunding);
    }
}
