package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// "PojazdZarezerwowanyZeStocku" (UC-INW-03/07, Fast Track) — auto z placu zablokowane pod zamówienie.
// Wyzwala w Fakturowaniu wystawienie faktury końcowej (UC-FIR-02).
public record VehicleReservedFromStockEvent(UUID eventId,
                                            String orderId,
                                            String vin,
                                            Instant occurredOn) implements DomainEvent {
}
