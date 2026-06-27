package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ConfiguratorSessionInitiatedEvent(String sessionId, String customerId, String salespersonId,
                                                UUID eventId, Instant occurredOn) implements DomainEvent {

    public ConfiguratorSessionInitiatedEvent(String sessionId, String customerId, String salespersonId) {
        this(sessionId, customerId, salespersonId, UUID.randomUUID(), Instant.now());
    }

    public ConfiguratorSessionInitiatedEvent(UUID eventId, String sessionId, String customerId,
                                             String salespersonId, Instant occurredOn) {
        this(sessionId, customerId, salespersonId, eventId, occurredOn);
    }
}
