package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "PojazduBrakNaStocku" (UC-INW-07, Long Track) — brak pasującego auta, powstaje slot produkcyjny.
// Wyzwala w Fakturowaniu prośbę o zadatek (UC-FIR-01).
public record VehicleIsNotOnStockEvent(UUID eventId,
                                       String orderId,
                                       Instant occurredOn) implements DomainEvent {
}
