package salon.financing.infrastructure.out.integration;

import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.port.out.SalesIntegration;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.SalesQueryFacade;

/**
 * ADAPTER WYJŚCIOWY (ACL, Rysunek 42: SalesQueryService) – realizacja portu {@link SalesIntegration}.
 *
 * Zależy wyłącznie od publicznej fasady Sprzedaży ({@link SalesQueryFacade}, Published Language)
 * i tłumaczy jej migawki na lokalny model Finansowania: CustomerSnapshotDto -> {@link BuyerDetails},
 * a cenę końcową oferty na kwotę do sfinansowania ({@link Money} ze Wspólnego Jądra).
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
    public BuyerDetails fetchBuyerDetails(String orderId) {
        CustomerSnapshotDto buyer = this.salesQueryFacade.findBuyerForOrder(new OrderId(orderId));
        return new BuyerDetails(buyer.fullName(), buyer.nip());
    }

    @Override
    public Money fetchAmountToFinance(String orderId) {
        return this.salesQueryFacade.findOfferForOrder(new OrderId(orderId)).finalPrice();
    }
}
