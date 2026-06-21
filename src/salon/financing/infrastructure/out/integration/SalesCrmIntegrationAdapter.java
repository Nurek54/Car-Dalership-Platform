package salon.financing.infrastructure.out.integration;

import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.port.out.SalesIntegration;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.SalesQueryFacade;

/**
 * ADAPTER WYJSCIOWY (ACL, Rysunek 42: SalesQueryService) — realizacja portu {@link SalesIntegration}.
 *
 * Warstwa Zapobiegajaca Uszkodzeniu: zalezy wylacznie od publicznej fasady Sprzedazy
 * ({@link SalesQueryFacade}, Jezyk Opublikowany) i tlumaczy jej migawki na lokalny model Finansowania:
 * CustomerSnapshotDto -> {@link BuyerDetails}, a cene koncowa oferty na kwote do sfinansowania
 * ({@link Money} ze Wspolnego Jadra).
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
    public BuyerDetails buyerDetails(String orderId) {
        CustomerSnapshotDto buyer = this.salesQueryFacade.findBuyerForOrder(new OrderId(orderId));
        return new BuyerDetails(buyer.fullName(), buyer.nip());
    }

    @Override
    public Money offerFinalPrice(String orderId) {
        return this.salesQueryFacade.findOfferForOrder(new OrderId(orderId)).finalPrice();
    }
}
