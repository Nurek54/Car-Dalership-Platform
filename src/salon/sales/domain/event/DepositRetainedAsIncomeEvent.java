package salon.sales.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

// UC-SPR-03: anulacja z winy klienta -> polecenie do Rozliczeń: zatrzymaj zadatek jako przychód.
public record DepositRetainedAsIncomeEvent(UUID eventId,
                                           String orderId,
                                           Instant occurredOn) implements DomainEvent {
}
