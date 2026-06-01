import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    void shouldSuccessfullyCancelOrderWhenCarIsNotHandedOverYet() {
        // Arrange
        Order order = new Order(
                new OrderId("ORD-001"),
                new OfferId("OFF-001")
        );
        boolean isHandedOver = false;

        // Act
        order.cancelOrder(CancellationReason.CLIENT_FAULT, isHandedOver);

        // Assert
        assertThat(order.getState()).isEqualTo(OrderState.CANCELLED);
        assertThat(order.getCancellationReason()).isEqualTo(CancellationReason.CLIENT_FAULT);
    }

    @Test
    void shouldThrowExceptionWhenTryingToCancelHandedOverCar() {
        // Arrange
        Order order = new Order(
                new OrderId("ORD-002"),
                new OfferId("OFF-002")
        );
        boolean isHandedOver = true; // Auto już wydane!

        // Act & Assert
        // Zgodnie z przypadkiem użycia UC-SPR-03, oczekujemy natychmiastowej blokady (wyjątku)
        assertThatThrownBy(() -> order.cancelOrder(CancellationReason.CLIENT_FAULT, isHandedOver))
                .isInstanceOf(IllegalStateException.class) // Lepszy byłby własny wyjątek, np. HandoverAlreadyCompletedException
                .hasMessageContaining("Cannot cancel order after the vehicle has been handed over");
    }
}