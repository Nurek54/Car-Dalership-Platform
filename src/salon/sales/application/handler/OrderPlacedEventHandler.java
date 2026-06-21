package salon.sales.application.handler;

import salon.sales.application.port.out.InventoryIntegration;
import salon.sales.application.domain.event.OrderPlacedEvent;

/**
 * Handler of the OrderPlaced event (UC-CRM-03): the newly placed order applies to
 * the Inventory Context for allocation of a vehicle from the yard or a production slot (UC-INW-01/02).
 */
public class OrderPlacedEventHandler {

    private final InventoryIntegration inventoryPort;

    public OrderPlacedEventHandler(InventoryIntegration inventoryPort) {
        this.inventoryPort = inventoryPort;
    }

    public void handle(OrderPlacedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.inventoryPort.allocateVehicleOrProductionSlot(event.orderId());
    }
}
