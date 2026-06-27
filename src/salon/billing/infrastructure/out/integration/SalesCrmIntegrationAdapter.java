package salon.billing.infrastructure.out.integration;

import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.application.port.out.SalesIntegration;
import salon.common.model.OrderId;
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
    public BuyerDetails buyerDetailsFor(String orderId) {
        CustomerSnapshotDto buyer = this.salesQueryFacade.findBuyerForOrder(new OrderId(orderId));
        return new BuyerDetails(buyer.fullName(), buyer.nip());
    }
}
