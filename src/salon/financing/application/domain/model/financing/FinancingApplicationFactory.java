package salon.financing.application.domain.model.financing;

import salon.common.model.Money;

/**
 * FABRYKA (Rysunek 42) — powoluje poprawny agregat {@link FinancingApplication} w stanie DRAFT,
 * nadajac mu nowy identyfikator wniosku ({@link ApplicationId}). Dane nabywcy i kwota finansowania
 * (cena koncowa oferty) dociagane sa przez warstwe aplikacji z Kontekstu Sprzedazy (SalesIntegration)
 * i przekazywane tu jako typy lokalne Finansowania. Operacja atomowa — nigdy nie zwraca niespojnego obiektu.
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
