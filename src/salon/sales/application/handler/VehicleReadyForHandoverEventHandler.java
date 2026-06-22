package salon.sales.application.handler;

import salon.common.model.OrderId;
import salon.sales.application.domain.event.VehicleReadyForHandoverEvent;
import salon.sales.application.service.SalesService;

/**
 * EVENT HANDLER (Figure 22) — reacts to the external {@link VehicleReadyForHandoverEvent} (UC-CRM-04).
 * The target port is the AppService, because we react to an external event by updating the aggregate.
 */
public class VehicleReadyForHandoverEventHandler {

    private final SalesService salesAppService;

    public VehicleReadyForHandoverEventHandler(SalesService salesAppService) {
        this.salesAppService = salesAppService;
    }

    public void handle(VehicleReadyForHandoverEvent event) {
        // We instruct the change of the order status
        this.salesAppService.markOrderAsReadyForHandover(new OrderId(event.orderId()));
    }
}
