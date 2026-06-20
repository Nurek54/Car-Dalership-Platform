package salon.sales.application.handler;

import salon.sales.application.port.out.ManufacturingIntegrationPort;
import salon.sales.application.domain.event.OrderActivatedEvent;

/**
 * Handler zdarzenia OrderActivated (UC-CRM-03 cz.2): zaksięgowana wpłata uruchamia
 * realizację pojazdu (np. zlecenie na taśmę produkcyjną — UC-INW-02).
 */
public class OrderActivatedEventHandler {

    private final ManufacturingIntegrationPort manufacturingPort;

    public OrderActivatedEventHandler(ManufacturingIntegrationPort manufacturingPort) {
        this.manufacturingPort = manufacturingPort;
    }

    public void handle(OrderActivatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.manufacturingPort.startVehicleRealization(event.orderId());
    }
}
