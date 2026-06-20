package salon.sales.infrastructure.in.messaging;

import salon.sales.application.service.SalesService;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń kompensacyjnych Inwentarza/Logistyki
 * w Kontekście Sprzedaży (UC-CRM-05 scenariusz A1).
 *
 * Ścieżkę "pojazd gotowy do wydania" (UC-CRM-04) obsługuje dedykowany
 * salon.sales.infrastructure.in.messaging.SalesEventSubscriberAdapter.
 */
public class LogisticsEventSubscriberAdapter {

    private final SalesService salesAppService;

    public LogisticsEventSubscriberAdapter(SalesService salesAppService) {
        if (salesAppService == null) {
            throw new IllegalArgumentException("salesAppService must not be null.");
        }
        this.salesAppService = salesAppService;
    }

    /** UC-CRM-05, A1: odmowa Inwentarza -> kompensata (powrót do "Gotowe do odbioru"). */
    public void handleVehicleInventoryReleasedError(VehicleInventoryReleasedError event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator zamówienia (orderId) nie może być pusty");
        }
        System.out.println("[LogisticsEventSubscriberAdapter] Blokada magazynowa dla zamówienia "
                + event.orderId() + " (" + event.reason() + ") -> cofam do READY_FOR_HANDOVER.");
        salesAppService.revertHandoverOnInventoryError(event.orderId());
    }
}
