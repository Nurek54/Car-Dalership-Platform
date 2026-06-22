package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-CRM-01: a new configurator session was opened for a customer. Outbound to the Catalog
 * and Configurator Context, which opens the configurator UI for the requested model year.
 */
public record InitiateConfiguratorSessionEvent(String sessionId, String customerId,
                                               String salespersonId, int modelYear,
                                               UUID eventId, Instant occurredOn) implements DomainEvent {

    public InitiateConfiguratorSessionEvent(String sessionId, String customerId,
                                            String salespersonId, int modelYear) {
        this(sessionId, customerId, salespersonId, modelYear, UUID.randomUUID(), Instant.now());
    }
}
