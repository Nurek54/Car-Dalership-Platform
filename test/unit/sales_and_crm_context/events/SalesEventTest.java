package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import salon.sales.domain.event.OrderCancelledEvent;
import salon.sales.domain.event.OrderPlacedEvent;
import salon.sales.domain.event.VehicleHandedOverEvent;
import salon.sales.domain.model.customer.CustomerId;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.order.CancellationReason;
import salon.sales.domain.model.order.Order;
import salon.sales.domain.model.order.OrderFactory;
import salon.shared.model.Money;
import salon.shared.model.OrderId;
import salon.shared.model.SpecificationId;

import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class SalesEventTest {

    @Test
    void shouldEmitOrderPlacedEventWhenOrderIsCreatedAndSigned() {
        Offer offer = new Offer(new OfferId("OFF-1"), new CustomerId("C-1"), new SpecificationId("S-1"));
        offer.publish(); // Zakładamy, że to emituje OfferPublishedEvent

        // Klient podpisuje umowę (UC-SPR-02)
        Order order = new OrderFactory().createFromOffer(offer.getId(), offer.toSnapshot());
        order.confirmSignature("DOCUSIGN-REF-123");

        assertThat(order.getDomainEvents())
                .hasAtLeastOneElementOfType(OrderPlacedEvent.class);
    }

    @Test
    void shouldEmitOrderCancelledEventWithReasonWhenCancelled() {
        Order order = new Order(new OrderId("ORD-1"), new OfferId("OFF-1"), Money.of(150000, "PLN"));

        // Anulowanie z winy klienta
        order.cancelOrder(CancellationReason.CLIENT_FAULT, false);

        assertThat(order.getDomainEvents())
                .hasAtLeastOneElementOfType(OrderCancelledEvent.class);

        // Sprawdzamy, czy zdarzenie ma odpowiednie dane
        OrderCancelledEvent event = (OrderCancelledEvent) order.getDomainEvents().get(0);
        assertThat(event.getReason()).isEqualTo("CLIENT_FAULT");
    }

    @Test
    void shouldEmitVehicleHandedOverEventWhenHandoverCompletes() {
        // Doprowadzamy zamówienie do stanu HANDOVER_SCHEDULED
        Order order = new Order(new OrderId("ORD-2"), new OfferId("OFF-2"), Money.of(150000, "PLN"));
        order.confirmSignature("SIG-2");
        order.activate();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now().plusDays(1));

        // Wydanie auta
        order.completeHandover();

        assertThat(order.getDomainEvents())
                .hasAtLeastOneElementOfType(VehicleHandedOverEvent.class);
    }
}