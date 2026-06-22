package salon.sales.application.handler;

import salon.sales.application.domain.event.OrderPlacedEvent;
import salon.sales.application.port.out.InventoryIntegration;

/**
 * EVENT HANDLER (Figure 22) — reacts to {@link OrderPlacedEvent} (UC-CRM-03):
 * asks Inventory to reserve a vehicle from stock or a production slot.
 */
public class OrderPlacedEventHandler {

    private final InventoryIntegration inventoryPort;

    public OrderPlacedEventHandler(InventoryIntegration inventoryPort) {
        this.inventoryPort = inventoryPort;
    }

    public void handle(OrderPlacedEvent event) {
        // We communicate with Inventory to reserve a slot
        this.inventoryPort.allocateVehicleOrProductionSlot(event.orderId());
    }
}
