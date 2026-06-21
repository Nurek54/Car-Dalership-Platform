package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "InitiateConfiguratorSession" (UC-CRM-01) — Sales initiates a configurator session for the customer.
 * The event is listened to by the Catalog Context, which opens the vehicle configurator interface.
 */
public record ConfiguratorSessionInitiatedEvent(UUID eventId,
                                                String sessionId,
                                                String customerId,
                                                String salespersonId,
                                                Instant occurredOn) implements DomainEvent {
}
