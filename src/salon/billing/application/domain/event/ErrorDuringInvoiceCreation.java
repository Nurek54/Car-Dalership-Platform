package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "BladWystawieniaFaktury" (UC-FIR-02, scenariusz A1) — system nie mógł utworzyć
 * dokumentu/pliku PDF faktury końcowej.
 */
public record ErrorDuringInvoiceCreation(UUID eventId,
                                         String orderId,
                                         String reason,
                                         Instant occurredOn) implements DomainEvent {
}
