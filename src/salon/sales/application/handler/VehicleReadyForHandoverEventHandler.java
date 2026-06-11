package salon.sales.application.handler;

import salon.sales.application.service.SalesAppService;
import salon.sales.domain.event.VehicleReadyForHandoverEvent;
import salon.shared.model.OrderId;

/**
 * Handler zdarzenia VehicleReadyForHandover z Kontekstu Inwentarza (UC-CRM-04, krok 1):
 * reagujemy na zdarzenie z zewnątrz, aktualizując stan agregatu przez usługę aplikacyjną.
 */
public class VehicleReadyForHandoverEventHandler {

    private final SalesAppService salesAppService;

    public VehicleReadyForHandoverEventHandler(SalesAppService salesAppService) {
        this.salesAppService = salesAppService;
    }

    public void handle(VehicleReadyForHandoverEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.salesAppService.markOrderAsReadyForHandover(new OrderId(event.orderId()));
    }
}
