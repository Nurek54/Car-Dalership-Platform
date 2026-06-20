package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "TerminPlatnosciMinal" (WF-FIR-03: monitorowanie i egzekwowanie terminów płatności) —
 * minął termin zapłaty (dueDate dokumentu), a saldo zamówienia nie zostało pokryte.
 * Nasłuchuje Kontekst Inwentarza i Logistyki: zdarzenie wyzwala UC-INW-04
 * (zwolnienie blokady pojazdu) — Inwentarz nie ma własnych timerów i polega na tym sygnale.
 */
public record PaymentDeadlineExpiredEvent(UUID eventId,
                                          String orderId,
                                          Instant occurredOn) implements DomainEvent {
}
