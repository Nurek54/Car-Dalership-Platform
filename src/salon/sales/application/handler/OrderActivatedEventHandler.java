package salon.sales.application.handler;

import salon.sales.application.domain.event.OrderActivatedEvent;
import salon.sales.application.port.out.ManufacturingIntegrationPort;

public class OrderActivatedEventHandler {

    private final ManufacturingIntegrationPort manufacturingPort;

    public OrderActivatedEventHandler(ManufacturingIntegrationPort manufacturingPort) {
        this.manufacturingPort = manufacturingPort;
    }

    public void handle(OrderActivatedEvent event) {
        
        this.manufacturingPort.startVehicleRealization(event.orderId());
    }
}
