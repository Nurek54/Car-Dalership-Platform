package salon.sales.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "PojazdGotowyDoWydania" — komunikat z Kontekstu Inwentarza (UC-INW-05) odbierany
 * przez Sprzedaż/CRM: pojazd o danym VIN jest gotowy fizycznie i finansowo,
 * zamówienie ma przejść w stan "Gotowe do odbioru" (UC-CRM-04).
 */
public record VehicleReadyForHandoverEvent(UUID eventId,
                                           String vin,
                                           String orderId,
                                           Instant occurredOn) implements DomainEvent {
}
