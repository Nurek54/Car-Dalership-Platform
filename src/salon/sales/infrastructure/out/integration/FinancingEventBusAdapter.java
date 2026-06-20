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
 * Adapter wyjściowy portu {@link FinancingIntegrationPort} (UC-CRM-03, krok 5).
 *
 * Realizuje zapytanie o zdolność kredytową/leasingową przez magistralę zdarzeń:
 * publikuje FinancingRequestedEvent, który (zgodnie z kanwą Finansowania) konsumuje
 * Kontekst Finansowania, uruchamiając UC-FIN-01 (złożenie wniosku przez ACL banku).
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
