package salon.sales.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// UC-SPR-03: anulacja z winy salonu -> polecenie do Rozliczeń: zleć zwrot zadatku.
public record DepositRefundOrderedEvent(UUID eventId,
                                        String orderId,
                                        Instant occurredOn) implements DomainEvent {
}
