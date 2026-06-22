package salon.sales.api;

import salon.common.model.OrderId;

public interface SalesQueryFacade {

    CustomerSnapshotDto findBuyerForOrder(OrderId orderId);

    OfferSnapshotDto findOfferForOrder(OrderId orderId);
}
