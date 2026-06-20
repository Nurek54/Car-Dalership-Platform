package salon.financing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "WniosekOdrzuconyPrzezWalidacje" (UC-FIN-01, scenariusz A2) — system banku natychmiast
 * zwrócił błąd o braku/niepoprawności wymaganych danych (np. błędny NIP). Nasłuchuje
 * Sprzedaż/CRM, aby Handlowiec poprawił wniosek wraz z Klientem.
 */
public record FinancingApplicationFailed(UUID eventId,
                                         String orderId,
                                         String reason,
                                         Instant occurredOn) implements DomainEvent {
}
