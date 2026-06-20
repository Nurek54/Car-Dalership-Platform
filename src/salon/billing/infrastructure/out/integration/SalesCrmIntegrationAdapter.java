package salon.billing.infrastructure.out.integration;

import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.application.port.out.SalesIntegration;
import salon.common.model.OrderId;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.SalesQueryFacade;

/**
 * ADAPTER WYJSCIOWY (Rys. 48 — SalesQueryService, ACL) — realizacja portu {@link SalesIntegration}.
 *
 * Warstwa Zapobiegajaca Uszkodzeniu: zalezy wylacznie od publicznej fasady Sprzedazy
 * ({@link SalesQueryFacade}, Jezyk Opublikowany) i tlumaczy jej migawke CustomerSnapshotDto na
 * lokalny obiekt wartosci Fakturowania {@link BuyerDetails}. Jadro Fakturowania nie zna agregatow
 * Sprzedazy (Customer/Offer/Order).
 */
public class SalesCrmIntegrationAdapter implements SalesIntegration {

    private final SalesQueryFacade salesQueryFacade;

    public SalesCrmIntegrationAdapter(SalesQueryFacade salesQueryFacade) {
        if (salesQueryFacade == null) {
            throw new IllegalArgumentException("salesQueryFacade must not be null.");
        }
        this.salesQueryFacade = salesQueryFacade;
    }

    @Override
    public BuyerDetails buyerDetailsFor(String orderId) {
        CustomerSnapshotDto buyer = this.salesQueryFacade.findBuyerForOrder(new OrderId(orderId));
        return new BuyerDetails(buyer.fullName(), buyer.nip());
    }
}
