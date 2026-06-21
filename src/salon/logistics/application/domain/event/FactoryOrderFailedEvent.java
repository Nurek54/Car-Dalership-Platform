package salon.logistics.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "ProductionOrderRejected" (UC-INW-02, scenario A1) — the factory API returned an error
 * (e.g. a connection problem); the production order was not accepted.
 */
public record FactoryOrderFailedEvent(UUID eventId,
                                      String orderId,
                                      String reason,
                                      Instant occurredOn) implements DomainEvent {
}
