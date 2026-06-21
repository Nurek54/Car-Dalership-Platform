package salon.sales.infrastructure.in.messaging;

import salon.sales.application.service.SalesService;

/**
 * Driving adapter — subscriber of the compensating events of Inventory/Logistics
 * in the Sales Context (UC-CRM-05 scenario A1).
 *
 * The "vehicle ready for handover" path (UC-CRM-04) is handled by a dedicated
 * salon.sales.infrastructure.in.messaging.SalesEventSubscriberAdapter.
 */
public class LogisticsEventSubscriberAdapter {

    private final SalesService salesAppService;

    public LogisticsEventSubscriberAdapter(SalesService salesAppService) {
        if (salesAppService == null) {
            throw new IllegalArgumentException("salesAppService must not be null.");
        }
        this.salesAppService = salesAppService;
    }

    /** UC-CRM-05, A1: Inventory's refusal -> compensation (revert to "Ready for handover"). */
    public void handleVehicleInventoryReleasedError(VehicleInventoryReleasedError event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("The order identifier (orderId) cannot be empty");
        }
        System.out.println("[LogisticsEventSubscriberAdapter] Stock lock for order "
                + event.orderId() + " (" + event.reason() + ") -> cofam do READY_FOR_HANDOVER.");
        salesAppService.revertHandoverOnInventoryError(event.orderId());
    }
}
