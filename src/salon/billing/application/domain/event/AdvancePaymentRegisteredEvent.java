package salon.billing.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZadatekZaksiegowany" — pierwsza wpłata po prośbie o zadatek (UC-FIR-01) została
 * zaksięgowana. Nasłuchuje Kontekst Inwentarza i Logistyki: zdarzenie wyzwala UC-INW-02
 * (zlecenie produkcji pojazdu w fabryce).
 */
public record AdvancePaymentRegisteredEvent(UUID eventId,
                                            String settlementId,
                                            String orderId,
                                            Instant occurredOn) implements DomainEvent {
}
