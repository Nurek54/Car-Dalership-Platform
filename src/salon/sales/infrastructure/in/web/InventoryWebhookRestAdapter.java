package salon.sales.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.sales.application.service.SalesService;
import salon.common.model.OrderId;

/**
 * Driving adapter — webhook for events from the Inventory Context (UC-CRM-04, step 1).
 * An alternative (synchronous) channel for VehicleReadyForHandover alongside the queue subscriber.
 */
@RestController
@RequestMapping("/api/sales/webhooks/inventory")
public class InventoryWebhookRestAdapter {

    private final SalesService salesAppService;

    public InventoryWebhookRestAdapter(SalesService salesAppService) {
        if (salesAppService == null) {
            throw new IllegalArgumentException("salesAppService must not be null.");
        }
        this.salesAppService = salesAppService;
    }

    /** The vehicle arrived in the yard and is ready — the order transitions to "Ready for handover". */
    @PostMapping("/vehicle-ready")
    public ResponseEntity<Void> onVehicleReady(@RequestBody VehicleReadyWebhookRequest request) {
        salesAppService.markOrderAsReadyForHandover(new OrderId(request.orderId()));
        return ResponseEntity.ok().build();
    }

    /** Webhook DTO — fields of the VehicleReadyForHandover event from Inventory. */
    public record VehicleReadyWebhookRequest(String eventId, String vin,
                                             String orderId, String occurredOn) {
    }
}
