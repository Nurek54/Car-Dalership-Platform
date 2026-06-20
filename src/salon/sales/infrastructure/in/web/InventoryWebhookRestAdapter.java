package salon.sales.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.sales.application.service.SalesService;
import salon.common.model.OrderId;

/**
 * Adapter sterujący (driving) — webhook dla zdarzeń z Kontekstu Inwentarza (UC-CRM-04, krok 1).
 * Alternatywny (synchroniczny) kanał dla VehicleReadyForHandover obok subskrybenta kolejki.
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

    /** Pojazd zjechał na plac i jest gotowy — zamówienie przechodzi w "Gotowe do odbioru". */
    @PostMapping("/vehicle-ready")
    public ResponseEntity<Void> onVehicleReady(@RequestBody VehicleReadyWebhookRequest request) {
        salesAppService.markOrderAsReadyForHandover(new OrderId(request.orderId()));
        return ResponseEntity.ok().build();
    }

    /** DTO webhooka — pola zdarzenia VehicleReadyForHandover z Inwentarza. */
    public record VehicleReadyWebhookRequest(String eventId, String vin,
                                             String orderId, String occurredOn) {
    }
}
