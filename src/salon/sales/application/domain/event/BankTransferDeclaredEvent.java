package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;
import salon.common.model.Money;

import java.time.Instant;
import java.util.UUID;

/**
 * "BankTransferDeclared" (UC-CRM-03, step 5) — the customer chose payment by bank transfer.
 * Carries the declared contract amount; listeners: the Billing Context (proforma
 * with transfer details — the BankTransferDeclaredEventHandler) and Inventory
 * (UC-INW-01 — availability check and vehicle reservation).
 */
public record BankTransferDeclaredEvent(UUID eventId,
                                        String orderId,
                                        Money declaredAmount,
                                        Instant occurredOn) implements DomainEvent {
}
