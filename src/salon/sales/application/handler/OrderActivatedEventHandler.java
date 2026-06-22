package salon.sales.application.handler;

import salon.sales.application.domain.event.OrderActivatedEvent;
import salon.sales.application.port.out.ManufacturingIntegrationPort;

/**
 * EVENT HANDLER (Figure 22) — reacts to {@link OrderActivatedEvent} (UC-CRM-03 part 2):
 * triggers vehicle realization (production line) once the payment was posted.
 */
public class OrderActivatedEventHandler {

    private final ManufacturingIntegrationPort manufacturingPort;

    public OrderActivatedEventHandler(ManufacturingIntegrationPort manufacturingPort) {
        this.manufacturingPort = manufacturingPort;
    }

    public void handle(OrderActivatedEvent event) {
        // Fulfillment starts (e.g. an order onto the production line)
        this.manufacturingPort.startVehicleRealization(event.orderId());
    }
}
