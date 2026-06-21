package salon.sales.application.handler;

import salon.sales.application.service.SalesService;
import salon.sales.application.domain.event.VehicleReadyForHandoverEvent;
import salon.common.model.OrderId;

/**
 * Handler of the VehicleReadyForHandover event from the Inventory Context (UC-CRM-04, step 1):
 * we react to an external event, updating the aggregate state through the application service.
 */
public class VehicleReadyForHandoverEventHandler {

    private final SalesService salesAppService;

    public VehicleReadyForHandoverEventHandler(SalesService salesAppService) {
        this.salesAppService = salesAppService;
    }

    public void handle(VehicleReadyForHandoverEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.salesAppService.markOrderAsReadyForHandover(new OrderId(event.orderId()));
    }
}
