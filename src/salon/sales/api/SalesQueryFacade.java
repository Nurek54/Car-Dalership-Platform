package salon.sales.api;

import salon.common.model.OrderId;

/**
 * Public API (facade) of the Sales and CRM Context for synchronous queries
 * from other contexts. The only — besides domain events — legal entry point
 * into the Sales context; it hides the repositories, aggregates and navigation
 * order -> offer -> customer.
 *
 * In a distributed environment this contract maps 1:1 to the REST endpoint
 * of the CRM module.
 */
public interface SalesQueryFacade {

    /**
     * Returns a snapshot of the buyer data for the indicated order.
     *
     * @throws IllegalArgumentException when orderId is null
     * @throws IllegalStateException    when the order, offer or customer does not exist
     */
    CustomerSnapshotDto findBuyerForOrder(OrderId orderId);

    /**
     * Returns a snapshot of the source offer (including the final price) for the indicated order.
     *
     * @throws IllegalArgumentException when orderId is null
     * @throws IllegalStateException    when the order or the offer does not exist,
     *                                  or the offer does not yet have a final price
     */
    OfferSnapshotDto findOfferForOrder(OrderId orderId);
}
