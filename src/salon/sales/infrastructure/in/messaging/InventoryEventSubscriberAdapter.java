package salon.sales.infrastructure.in.messaging;

import salon.sales.application.service.SalesService;

/**
 * INBOUND ADAPTER (Figure 22: EventListener) — subscriber of the Inventory and Logistics Context.
 *
 * Handles two paths:
 *  - VehicleReadyForHandover -> mark the order ready for handover (UC-CRM-04),
 *  - VehicleReleaseFailed   -> revert the order to READY_FOR_HANDOVER (UC-CRM-05 / A1).
 * ACL: Inventory messages are translated into simple local records.
 */
public class InventoryEventSubscriberAdapter {

    private final SalesService salesService;

    public InventoryEventSubscriberAdapter(SalesService salesService) {
        if (salesService == null) {
            throw new IllegalArgumentException("salesService must not be null.");
        }
        this.salesService = salesService;
    }

    public void handleVehicleReadyForHandover(VehicleReadyForHandover event) {
        if (event == null || event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        this.salesService.markReadyForHandover(event.orderId());
    }

    public void handleVehicleReleaseFailed(VehicleReleaseFailed event) {
        if (event == null || event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        this.salesService.revertHandoverOnInventoryError(event.orderId());
    }

    /** Local (ACL) representations of messages from the Inventory and Logistics Context. */
    public record VehicleReadyForHandover(String orderId) {
    }

    public record VehicleReleaseFailed(String orderId) {
    }
}
