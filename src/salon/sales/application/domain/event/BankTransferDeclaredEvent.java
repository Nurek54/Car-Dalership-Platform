package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-CRM-03: the customer declared a bank-transfer payment. Outbound to Inventory and Logistics,
 * which starts the vehicle reservation from stock (UC-INW-01).
 */
public record BankTransferDeclaredEvent(String orderId,
                                        UUID eventId, Instant occurredOn) implements DomainEvent {

    public BankTransferDeclaredEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
