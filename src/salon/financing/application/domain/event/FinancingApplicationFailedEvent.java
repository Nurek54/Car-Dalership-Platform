package salon.financing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-FIN-01 / A2: the bank immediately rejected the application due to incorrect/incomplete data
 * (e.g. an incorrect tax ID). The event returns to the Sales and CRM Context so the salesperson can fix the application.
 */
public record FinancingApplicationFailedEvent(String orderId, String reason,
                                              UUID eventId, Instant occurredOn) implements DomainEvent {

    public FinancingApplicationFailedEvent(String orderId, String reason) {
        this(orderId, reason, UUID.randomUUID(), Instant.now());
    }
}
