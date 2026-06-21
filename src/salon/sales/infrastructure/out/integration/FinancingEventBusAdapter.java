package salon.sales.infrastructure.out.integration;

import salon.sales.application.port.out.FinancingIntegrationPort;
import salon.sales.application.domain.event.FinancingRequestedEvent;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound adapter of the {@link FinancingIntegrationPort} port (UC-CRM-03, step 5).
 *
 * It implements the credit/leasing capacity query through the event bus:
 * it publishes FinancingRequestedEvent, which (per the Financing canvas) is consumed by
 * the Financing Context, triggering UC-FIN-01 (submitting the application through the bank ACL).
 */
public class FinancingEventBusAdapter implements FinancingIntegrationPort {

    private final EventPublisher eventPublisher;

    public FinancingEventBusAdapter(EventPublisher eventPublisher) {
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void requestFinancing(OrderId orderId, CustomerId customerId, Money amountToFinance) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        this.eventPublisher.publish(new FinancingRequestedEvent(
                UUID.randomUUID(), orderId.value(),
                customerId == null ? null : customerId.value(), Instant.now()));
    }
}
