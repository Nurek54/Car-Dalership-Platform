package salon.logistics.infrastructure.messaging;

import salon.logistics.application.port.in.HandleOrderEventsUseCase;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Sprzedaży w Kontekście Inwentarza i Logistyki.
 *
 * Odbiera "ZamówienieAktywowane/Złożone" i zleca alokację pojazdu (Fast/Long Track, UC-INW-02).
 * Waliduje wejście (uszkodzone dane -> odrzucenie); awarie infrastruktury przepuszcza wyżej (DLQ).
 */
public class SalesEventSubscriberAdapter {

    private final HandleOrderEventsUseCase orderEvents;

    public SalesEventSubscriberAdapter(HandleOrderEventsUseCase orderEvents) {
        if (orderEvents == null) {
            throw new IllegalArgumentException("orderEvents must not be null.");
        }
        this.orderEvents = orderEvents;
    }

    public void handleOrderPlacedEvent(OrderActivatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator zamówienia (orderId) nie może być pusty");
        }
        orderEvents.allocateVehicleForOrder(event.orderId(), event.specCodes());
    }
}
