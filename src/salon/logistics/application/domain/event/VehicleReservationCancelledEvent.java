package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-04: automatyczne zdjęcie blokady po przekroczeniu terminu płatności —
 * pojazd wraca na plac do ponownej sprzedaży.
 */
public record VehicleReservationCancelledEvent(String orderId, String vin,
                                               UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleReservationCancelledEvent(String orderId, String vin) {
        this(orderId, vin, UUID.randomUUID(), Instant.now());
    }
}
