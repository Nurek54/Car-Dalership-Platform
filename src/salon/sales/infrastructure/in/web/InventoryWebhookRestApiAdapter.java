package salon.sales.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.common.model.OrderId;
import salon.sales.application.service.SalesService;

@RestController
@RequestMapping("/api/sales/webhooks/inventory")
public class InventoryWebhookRestApiAdapter {

    private final SalesService salesService;

    public InventoryWebhookRestApiAdapter(SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping("/vehicle-ready")
    public ResponseEntity<Void> vehicleReady(@RequestBody VehicleReadyWebhook event) {
        this.salesService.markOrderAsReadyForHandover(new OrderId(event.orderId()));
        return ResponseEntity.ok().build();
    }

    
    public record VehicleReadyWebhook(String eventId, String vin, String orderId, String occurredOn) {
    }
}
