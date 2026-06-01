package salon.billing.infrastructure.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Zdarzenie PRZYCHODZĄCE (integracyjne): sygnał, że zamówienie jest gotowe do rozliczenia
 * (wyzwalacz UC-ROZ-03). Przychodzi asynchronicznie z innego kontekstu (np. Sprzedaż).
 * eventId służy do deduplikacji po stronie Subskrybenta (sekcja 3.4.2).
 */
public record OrderReadyForSettlementEvent(UUID eventId,
                                           String orderId,
                                           BigDecimal vehicleValue,
                                           BigDecimal totalDeposits,
                                           String currency,
                                           Instant occurredOn) {
}
