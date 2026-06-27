package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;
import salon.common.model.Money;

import java.time.Instant;
import java.util.UUID;

public record BankTransferDeclaredEvent(String orderId, Money amount,
                                        UUID eventId, Instant occurredOn) implements DomainEvent {

    public BankTransferDeclaredEvent(String orderId) {
        this(orderId, null, UUID.randomUUID(), Instant.now());
    }

    public BankTransferDeclaredEvent(String orderId, Money amount) {
        this(orderId, amount, UUID.randomUUID(), Instant.now());
    }

    public BankTransferDeclaredEvent(UUID eventId, String orderId, Money amount, Instant occurredOn) {
        this(orderId, amount, eventId, occurredOn);
    }
}
