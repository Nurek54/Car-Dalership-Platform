package salon.sales.application.handler;

import salon.sales.application.domain.event.OrderPlacedEvent;
import salon.sales.application.port.out.InventoryIntegration;

public class OrderPlacedEventHandler {

    private final InventoryIntegration inventoryPort;

    public OrderPlacedEventHandler(InventoryIntegration inventoryPort) {
        this.inventoryPort = inventoryPort;
    }

    public void handle(OrderPlacedEvent event) {
        
        this.inventoryPort.allocateVehicleOrProductionSlot(event.orderId());
    }
}
