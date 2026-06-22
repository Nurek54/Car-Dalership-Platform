package salon.sales.infrastructure.in.messaging;

import org.springframework.stereotype.Component;
import salon.common.model.OrderId;
import salon.sales.application.domain.event.VehicleReadyForHandoverEvent;
import salon.sales.application.service.SalesService;

/**
 * INBOUND ADAPTER (Figure 22: EventListener over the broker) — consumes Inventory/Logistics events
 * and drives the {@link SalesService}. Errors from the application layer are caught and logged so a
 * single poisoned message does not block (lock up) the whole queue.
 */
@Component
public class SalesEventSubscriberAdapter {

    private final SalesService salesAppService;

    public SalesEventSubscriberAdapter(SalesService salesAppService) {
        this.salesAppService = salesAppService;
    }

    /** VehicleReadyForHandover (UC-CRM-04): mark the order ready for handover. */
    public void onVehicleReadyForHandover(VehicleReadyForHandoverEvent event) {
        try {
            this.salesAppService.markOrderAsReadyForHandover(new OrderId(event.orderId()));
        } catch (RuntimeException ex) {
            // Log and swallow: do not crash the listener / block the queue on a single bad message.
            System.err.println("[SalesEventSubscriberAdapter] Failed to handle VehicleReadyForHandover for order "
                    + event.orderId() + ": " + ex.getMessage());
        }
    }
}
