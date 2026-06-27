package salon.financing.infrastructure.out.integration;

import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.port.out.SalesIntegration;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.SalesQueryFacade;

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
