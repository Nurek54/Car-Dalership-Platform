package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "BladProsbyOZadatek" (UC-FIR-01, scenariusz A1) — brak wymaganych informacji do
 * wygenerowania prośby o zadatek (np. brak danych nabywcy lub salda dla zamówienia).
 */
public record ErrorDuringPaymentRequest(UUID eventId,
                                        String orderId,
                                        String reason,
                                        Instant occurredOn) implements DomainEvent {
}
