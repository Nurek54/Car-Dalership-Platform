package salon.sales.application.domain.model.order;

import salon.common.event.DomainEvent;
import salon.common.model.OrderId;

import java.time.Instant;
import java.util.UUID;

/**
 * "ZawnioskowanoOFinansowanie" (UC-CRM-03, krok 5) — klient zadeklarował kredyt/leasing.
 * Zdarzenie rejestrowane przez agregat Order przy declarePaymentMethod(FINANCING);
 * jego odpowiednik integracyjny publikuje warstwa aplikacji (salon.sales.domain.event).
 */
public record FinancingRequestedEvent(UUID eventId,
                                      OrderId orderId,
                                      Instant occurredOn) implements DomainEvent {

    public OrderId getOrderId() {
        return this.orderId;
    }
}
