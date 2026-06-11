package salon.logistics.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "PojazdDostarczonyNaStan" (UC-INW-03) — fizyczny pojazd z fabryki dotarł na plac
 * i został sparowany z oczekującym zamówieniem (status: Zarezerwowany).
 * Nasłuchują: Sprzedaż i CRM oraz Fakturowanie i rozliczenia (wg kanwy Inwentarza).
 */
public record VehicleDeliveredToStockEvent(UUID eventId,
                                           String orderId,
                                           String vin,
                                           Instant occurredOn) implements DomainEvent {
}
