package salon.sales.infrastructure.in.messaging;

import org.springframework.stereotype.Component;
import salon.sales.application.service.SalesService;
import salon.sales.application.domain.event.VehicleReadyForHandoverEvent;
import salon.common.model.OrderId;

/**
 * Adapter sterujący (driving) — subskrybent komunikatów z Kontekstu Inwentarza
 * w Kontekście Sprzedaży (UC-CRM-04, krok 1: VehicleReadyForHandover).
 *
 * Adapter wyłapuje błędy warstwy aplikacji i loguje je, NIE wysadzając nasłuchu —
 * pojedynczy zatruty komunikat nie może zablokować całej kolejki.
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

    /** UC-CRM-04: pojazd gotowy fizycznie i finansowo -> zamówienie "Gotowe do odbioru". */
    public void onVehicleReadyForHandover(VehicleReadyForHandoverEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        try {
            salesAppService.markOrderAsReadyForHandover(new OrderId(event.orderId()));
        } catch (Exception e) {
            System.err.println("[SalesEventSubscriberAdapter] Nie udało się przetworzyć zdarzenia "
                    + event.eventId() + ": " + e.getMessage());
        }
    }
}
