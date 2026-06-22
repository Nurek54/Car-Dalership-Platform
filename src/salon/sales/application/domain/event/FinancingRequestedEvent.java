package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-CRM-03: the customer chose financing/leasing. Outbound to the Financing Context, which
 * submits the credit application to the bank (UC-FIN-01).
 */
public record FinancingRequestedEvent(String orderId, String customerId,
                                      UUID eventId, Instant occurredOn) implements DomainEvent {

    public FinancingRequestedEvent(String orderId, String customerId) {
        this(orderId, customerId, UUID.randomUUID(), Instant.now());
    }
}
