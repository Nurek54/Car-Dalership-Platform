package unit.sales_and_crm_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.domain.model.order.OrderState;
import salon.sales.application.domain.model.offer.OfferId;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.sales.application.domain.event.OrderCompletedEvent;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/** UC-CRM-05: Registering the physical vehicle handover */
class ReleaseVehicleDomainTest {

    // Metoda pomocnicza
    private Order prepareScheduledOrder() {
        Order order = new Order(new OrderId("ORD-999"), new OfferId("OFF-999"), Money.of(150000, "PLN"));
        order.activate();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now().plusDays(1)); // State: HANDOVER_SCHEDULED
        order.pullDomainEvents();
        return order;
    }

    @Test
    void shouldConfirmHandoverAndCompleteOrder() { // MAIN SCENARIO
        // The handover is scheduled
        Order order = prepareScheduledOrder();

        // The user confirms the physical handover of the keys to the customer
        order.confirmHandover();

        // The aggregate transitions to the final, immutable COMPLETED state
        assertThat(order.state()).isEqualTo(OrderState.COMPLETED);
        // A closing event is generated
        assertThat(order.getDomainEvents()).hasAtLeastOneElementOfType(OrderCompletedEvent.class);
    }

    @Test
    void shouldRevertToReadyForHandoverWhenInventoryReleaseFails() {

        Order order = prepareScheduledOrder();
        order.confirmHandover(); // Final state: COMPLETED
        order.pullDomainEvents();

        // The CRM module receives an error event from the Inventory module (VehicleInventoryReleasedError),
        order.revertToReadyForHandover();

        assertThat(order.state()).isEqualTo(OrderState.READY_FOR_HANDOVER);

        assertThat(order.handoverDate()).isNull();
    }
}