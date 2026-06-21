package salon.sales.infrastructure.in.messaging;

import org.springframework.stereotype.Component;
import salon.sales.application.service.SalesService;
import salon.sales.application.domain.event.VehicleReadyForHandoverEvent;
import salon.common.model.OrderId;

/**
 * Driving adapter — subscriber of messages from the Inventory Context
 * in the Sales Context (UC-CRM-04, step 1: VehicleReadyForHandover).
 *
 * The adapter catches application-layer errors and logs them, WITHOUT blowing up the listener —
 * a single poisoned message must not block the whole queue.
 */
@Component
public class SalesEventSubscriberAdapter {

    private final SalesService salesAppService;

    public SalesEventSubscriberAdapter(SalesService salesAppService) {
        if (salesAppService == null) {
            throw new IllegalArgumentException("salesAppService must not be null.");
        }
        this.salesAppService = salesAppService;
    }

    /** UC-CRM-04: the vehicle is ready physically and financially -> order "Ready for handover". */
    public void onVehicleReadyForHandover(VehicleReadyForHandoverEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        try {
            salesAppService.markOrderAsReadyForHandover(new OrderId(event.orderId()));
        } catch (Exception e) {
            System.err.println("[SalesEventSubscriberAdapter] Failed to process the event "
                    + event.eventId() + ": " + e.getMessage());
        }
    }
}
