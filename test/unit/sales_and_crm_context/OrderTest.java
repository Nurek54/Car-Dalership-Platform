package unit.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.order.CancellationReason;
import salon.sales.domain.model.order.Order;
import salon.sales.domain.model.order.OrderState;
import salon.shared.model.OrderId;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    void shouldSuccessfullyCancelOrderWhenCarIsNotHandedOverYet() {
        Order order = new Order(new OrderId("ORD-001"), new OfferId("OFF-001"));
        boolean isHandedOver = false;

        order.cancelOrder(CancellationReason.CLIENT_FAULT, isHandedOver);

        assertThat(order.getState()).isEqualTo(OrderState.CANCELLED);
        assertThat(order.getCancellationReason()).isEqualTo(CancellationReason.CLIENT_FAULT);
    }

    @Test
    void shouldThrowExceptionWhenTryingToCancelHandedOverCar() {
        Order order = new Order(new OrderId("ORD-002"), new OfferId("OFF-002"));
        boolean isHandedOver = true;

        assertThatThrownBy(() -> order.cancelOrder(CancellationReason.CLIENT_FAULT, isHandedOver))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel order after the vehicle has been handed over");
    }
}
