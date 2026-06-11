package salon.sales.domain.event;

import salon.shared.event.DomainEvent;
import salon.shared.model.Money;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZadeklarowanoPrzelew" (UC-CRM-03, krok 5) — klient wybrał płatność przelewem.
 * Niesie zadeklarowaną kwotę kontraktu; nasłuchują: Kontekst Rozliczeń (proforma
 * z danymi do przelewu — handler BankTransferDeclaredEventHandler) oraz Inwentarz
 * (UC-INW-01 — weryfikacja dostępności i rezerwacja pojazdu).
 */
public record BankTransferDeclaredEvent(UUID eventId,
                                        String orderId,
                                        Money declaredAmount,
                                        Instant occurredOn) implements DomainEvent {
}
