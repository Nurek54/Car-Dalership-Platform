package salon.sales.application.domain.model.order;

import salon.common.event.DomainEvent;
import salon.common.model.OrderId;

import java.time.Instant;
import java.util.UUID;

/**
 * "FinancingRequested" (UC-CRM-03, step 5) — the customer declared credit/leasing.
 * The event is recorded by the Order aggregate on declarePaymentMethod(FINANCING);
 * its integration counterpart is published by the application layer (salon.sales.domain.event).
 */
public record FinancingRequestedEvent(UUID eventId,
                                      OrderId orderId,
                                      Instant occurredOn) implements DomainEvent {

    public OrderId orderId() {
        return this.orderId;
    }
}
