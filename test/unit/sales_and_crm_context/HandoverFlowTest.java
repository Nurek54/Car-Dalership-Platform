package unit.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import salon.sales.domain.event.OrderReadyForHandoverEvent;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.order.Order;
import salon.sales.domain.model.order.OrderState;
import salon.shared.model.OrderId;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/** UC-CRM-04: gotowość pojazdu i umówienie odbioru. */
class HandoverFlowTest {

    private static Order inProgressOrder(String id) {
        Order order = new Order(new OrderId(id), new OfferId("OFF-" + id));
        order.confirmSignature("SIG-" + id);   // DRAFT_CREATED -> PENDING_PAYMENT
        order.activate();                       // PENDING_PAYMENT -> IN_PROGRESS
        order.pullDomainEvents();               // wyczyść zdarzenia z wcześniejszych kroków
        return order;
    }

    @Test
    void shouldBecomeReadyForHandoverAndEmitEvent() {
        Order order = inProgressOrder("ORD-10");

        order.markAsReadyForHandover();

        assertThat(order.getState()).isEqualTo(OrderState.READY_FOR_HANDOVER);
        assertThat(order.getDomainEvents()).hasAtLeastOneElementOfType(OrderReadyForHandoverEvent.class);
    }

    @Test
    void shouldScheduleHandoverWhenReady() {
        Order order = inProgressOrder("ORD-11");
        order.markAsReadyForHandover();
        LocalDate date = LocalDate.now().plusDays(3);

        order.scheduleHandover(date);

        assertThat(order.getState()).isEqualTo(OrderState.HANDOVER_SCHEDULED);
        assertThat(order.getHandoverDate()).isEqualTo(date);
    }

    @Test
    void shouldRejectReadyForHandoverWhenNotInProgress() {
        Order fresh = new Order(new OrderId("ORD-12"), new OfferId("OFF-12")); // DRAFT_CREATED

        assertThatThrownBy(fresh::markAsReadyForHandover)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectScheduleHandoverBeforeReady() {
        Order order = inProgressOrder("ORD-13"); // IN_PROGRESS, jeszcze nie READY

        assertThatThrownBy(() -> order.scheduleHandover(LocalDate.now().plusDays(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
