package salon.sales.infrastructure.in.messaging;

import salon.sales.application.service.SalesService;

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

    public record VehicleReadyForHandover(String orderId) {
    }

    public record VehicleReleaseFailed(String orderId) {
    }
}
