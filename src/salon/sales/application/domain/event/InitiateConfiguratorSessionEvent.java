package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InitiateConfiguratorSessionEvent(String sessionId, String customerId,
                                               String salespersonId, int modelYear,
                                               UUID eventId, Instant occurredOn) implements DomainEvent {

    public InitiateConfiguratorSessionEvent(String sessionId, String customerId,
                                            String salespersonId, int modelYear) {
        this(sessionId, customerId, salespersonId, modelYear, UUID.randomUUID(), Instant.now());
    }
}
