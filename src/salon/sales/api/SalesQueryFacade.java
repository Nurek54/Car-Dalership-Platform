package salon.sales.api;

import salon.common.model.OrderId;

/**
 * Public API (facade) of the Sales and CRM Context for synchronous queries from other contexts
 * (Figure 22 — "SalesQueryFacade"). Together with domain events it is the only legal entry point
 * into Sales; it hides repositories, aggregates and the order -> offer -> customer navigation.
 *
 * In a distributed setup this contract maps 1:1 onto a CRM REST endpoint.
 */
public interface SalesQueryFacade {

    /**
     * Returns a snapshot of the buyer for the given order.
     *
     * @throws IllegalArgumentException if orderId is null
     * @throws IllegalStateException    if the order, offer or customer does not exist
     */
    CustomerSnapshotDto findBuyerForOrder(OrderId orderId);

    /**
     * Returns a snapshot of the source offer (incl. final price) for the given order.
     *
     * @throws IllegalArgumentException if orderId is null
     * @throws IllegalStateException    if the order or offer does not exist
     */
    OfferSnapshotDto findOfferForOrder(OrderId orderId);
}
