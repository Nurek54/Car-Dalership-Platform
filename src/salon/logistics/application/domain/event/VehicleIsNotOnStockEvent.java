package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-INW-01 / A1: brak wolnego pojazdu o wymaganej specyfikacji na placu — rezerwacja
 * wstrzymana. Wyzwala ścieżkę zamówienia produkcji / prośby o zadatek (UC-FIR-01).
 */
public record VehicleIsNotOnStockEvent(String orderId,
                                       UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleIsNotOnStockEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
