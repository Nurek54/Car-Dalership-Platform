package salon.sales.infrastructure.integration;

import salon.sales.application.port.out.FinancingIntegrationPort;
import salon.sales.domain.event.FinancingRequestedEvent;
import salon.sales.domain.model.customer.CustomerId;
import salon.shared.application.EventPublisherPort;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

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

    private final EventPublisherPort eventPublisher;

    public FinancingEventBusAdapter(EventPublisherPort eventPublisher) {
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
                UUID.randomUUID(), orderId.value(), Instant.now()));
    }
}
