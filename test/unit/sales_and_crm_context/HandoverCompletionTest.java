package unit.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import salon.sales.domain.event.VehicleHandedOverEvent;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.order.Order;
import salon.sales.domain.model.order.OrderState;
import salon.shared.model.OrderId;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/** UC-CRM-05: rejestracja fizycznego wydania pojazdu (z A1 — odmowa Inwentarza). */
class HandoverCompletionTest {

    private static Order scheduledOrder(String id) {
        Order order = new Order(new OrderId(id), new OfferId("OFF-" + id));
        order.confirmSignature("SIG-" + id);
        order.activate();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now().plusDays(2));
        order.pullDomainEvents();
        return order;
    }

    @Test
    void shouldCompleteHandoverAndEmitVehicleHandedOverEvent() {
        Order order = scheduledOrder("ORD-20");

        order.completeHandover();

        assertThat(order.getState()).isEqualTo(OrderState.COMPLETED);
        assertThat(order.getDomainEvents()).hasAtLeastOneElementOfType(VehicleHandedOverEvent.class);
    }

    @Test
    void shouldRejectCompletionWhenNotScheduled() {
        Order order = new Order(new OrderId("ORD-21"), new OfferId("OFF-21")); // DRAFT_CREATED

        assertThatThrownBy(order::completeHandover)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRevertToReadyForHandoverOnInventoryError() {
        Order order = scheduledOrder("ORD-22");
        order.completeHandover(); // COMPLETED

        order.revertToReadyForHandover(); // A1 — odmowa Inwentarza

        assertThat(order.getState()).isEqualTo(OrderState.READY_FOR_HANDOVER);
    }
}
