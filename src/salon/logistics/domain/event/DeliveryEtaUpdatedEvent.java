package salon.logistics.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// "StatusDostawyZaktualizowany" (UC-INW-04) — nowa estymowana data dostawy ze slotu produkcyjnego.
public record DeliveryEtaUpdatedEvent(UUID eventId,
                                      String orderId,
                                      LocalDate estimatedDelivery,
                                      Instant occurredOn) implements DomainEvent {
}
