package salon.billing.domain.event;

import salon.shared.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * "WplataZaksiegowana" (UC-FIR-03, krok 4) — przelew sparowany z zamówieniem i dopisany
 * do agregatu Settlement. Nasłuchuje m.in. Sprzedaż/CRM (aktywacja zamówienia po
 * pierwszej wpłacie — UC-CRM-03 cz.2).
 */
public record PaymentRegisteredEvent(UUID eventId,
                                     String settlementId,
                                     String orderId,
                                     BigDecimal amount,
                                     String currency,
                                     Instant occurredOn) implements DomainEvent {
}
