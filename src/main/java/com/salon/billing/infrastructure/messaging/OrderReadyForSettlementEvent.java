package main.java.com.salon.billing.infrastructure.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Zdarzenie PRZYCHODZĄCE (integracyjne): sygnał, że zamówienie jest gotowe do rozliczenia
 * (wyzwalacz UC-ROZ-03). Zgodnie z sekcją 3.4 przychodzi asynchronicznie z innego kontekstu
 * (np. Sprzedaż) przez broker i jest odbierane przez SettlementEventListener.
 *
 * eventId służy do deduplikacji po stronie Subskrybenta (sekcja 3.4.2).
 *
 * Uwaga: dla uproszczenia szkieletu zdarzenie niesie komplet danych potrzebnych do komendy.
 * W realnym systemie część z nich (np. suma zadatków) pochodziłaby z naszego własnego kontekstu.
 */
public record OrderReadyForSettlementEvent(UUID eventId,
                                           String orderId,
                                           BigDecimal vehicleValue,
                                           BigDecimal totalDeposits,
                                           String currency,
                                           Instant occurredOn) {
}
