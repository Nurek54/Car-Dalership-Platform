package salon.sales.application.handler;

import salon.sales.application.port.out.ManufacturingIntegrationPort;
import salon.sales.application.domain.event.OrderActivatedEvent;

/**
 * Handler of the OrderActivated event (UC-CRM-03 part 2): a posted payment triggers
 * the vehicle fulfillment (e.g. an order onto the production line — UC-INW-02).
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
