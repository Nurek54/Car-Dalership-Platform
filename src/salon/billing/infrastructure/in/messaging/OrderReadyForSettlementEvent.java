package salon.billing.infrastructure.in.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Zdarzenie PRZYCHODZĄCE (integracyjne): złożenie nowego zamówienia gotowego do rozliczenia
 * (wyzwalacz inicjalizacji UC-FIR-03). Przychodzi asynchronicznie z innego kontekstu (Sprzedaż).
 * eventId służy do deduplikacji po stronie Subskrybenta.
 */
public record OrderReadyForSettlementEvent(UUID eventId,
                                           String orderId,
                                           BigDecimal contractValue,
                                           String currency,
                                           Instant occurredOn) {
}
