package salon.sales.application.handler;

import salon.sales.application.port.out.InventoryIntegrationPort;
import salon.sales.domain.event.OrderPlacedEvent;

/**
 * Handler zdarzenia OrderPlaced (UC-CRM-03): nowo złożone zamówienie zgłasza się do
 * Kontekstu Inwentarza po alokację pojazdu z placu lub slotu produkcyjnego (UC-INW-01/02).
 */
public class OrderPlacedEventHandler {

    private final InventoryIntegrationPort inventoryPort;

    public OrderPlacedEventHandler(InventoryIntegrationPort inventoryPort) {
        this.inventoryPort = inventoryPort;
    }

    public void handle(OrderPlacedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.inventoryPort.allocateVehicleOrProductionSlot(event.orderId());
    }
}
