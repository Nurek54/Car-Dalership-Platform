package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record BankTransferDeclaredEvent(String orderId,
                                        UUID eventId, Instant occurredOn) implements DomainEvent {

    public BankTransferDeclaredEvent(String orderId) {
        this(orderId, UUID.randomUUID(), Instant.now());
    }
}
