package salon.sales.application.handler;

import salon.common.model.OrderId;
import salon.sales.application.domain.event.VehicleReadyForHandoverEvent;
import salon.sales.application.service.SalesService;

public class VehicleReadyForHandoverEventHandler {

    private final SalesService salesAppService;

    public VehicleReadyForHandoverEventHandler(SalesService salesAppService) {
        this.salesAppService = salesAppService;
    }

    public void handle(VehicleReadyForHandoverEvent event) {
        
        this.salesAppService.markOrderAsReadyForHandover(new OrderId(event.orderId()));
    }
}
