package salon.sales.infrastructure.messaging;

import salon.sales.application.service.OrderAppService;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Inwentarza/Logistyki w Kontekście Sprzedaży.
 *
 * Odbiera "VehicleReadyForHandoverEvent" (UC-CRM-04) i zleca przejście zamówienia w stan
 * "Gotowe do odbioru". Waliduje wejście (uszkodzone dane z kolejki -> odrzucenie); awarie
 * warstwy aplikacji przepuszcza wyżej, by broker (RabbitMQ) skierował wiadomość do DLQ.
 */
public class LogisticsEventSubscriberAdapter {

    private final OrderAppService orderAppService;

    public LogisticsEventSubscriberAdapter(OrderAppService orderAppService) {
        if (orderAppService == null) {
            throw new IllegalArgumentException("orderAppService must not be null.");
        }
        this.orderAppService = orderAppService;
    }

    public void handleVehicleReadyForHandover(VehicleReadyForHandoverEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator zamówienia (orderId) nie może być pusty");
        }
        orderAppService.markReadyForHandover(event.orderId());
    }
}
