package salon.logistics.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "BladWydaniaPojazdu" (UC-INW-06, A1) — komenda ReleaseVehicle odrzucona (pojazd w stanie
 * innym niż "Gotowy do wydania"). Nasłuchuje Sprzedaż/CRM: kompensata cofa zamówienie
 * do "Gotowe do odbioru" (UC-CRM-05, A1).
 */
public record VehicleInventoryReleasedError(UUID eventId,
                                            String orderId,
                                            String reason,
                                            Instant occurredOn) implements DomainEvent {
}
