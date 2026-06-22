package unit.sales_and_crm_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.sales.application.domain.model.order.InvalidOrderStateException;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.domain.model.order.OrderState;
import salon.sales.application.domain.model.order.PaymentStatus;
import salon.sales.application.domain.model.offer.OfferId;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.sales.application.domain.event.OrderReadyForHandoverEvent;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/** UC-CRM-04: Handling the customer's invitation for pickup */
class ScheduleHandoverDomainTest {

    // Helper method - simulates an order that is in production (IN_PROGRESS)
    private Order prepareInProgressOrder() {
        Order order = new Order(new OrderId("ORD-10"), new OfferId("OFF-10"), Money.of(150000, "PLN"));
        order.activate(); // Transition from DRAFT_CREATED to IN_PROGRESS
        order.pullDomainEvents(); // We clear the event list before the actual test
        return order;
    }

    @Test
    void shouldBecomeReadyForHandoverAndEmitEvent() { // MAIN SCENARIO
        // The order is in progress (the factory produced the car)
        Order order = prepareInProgressOrder();

        // The logistics system reports that the car came off the transporter and is in the yard
        order.markAsReadyForHandover();

        // The order state allows handover (READY_FOR_HANDOVER)
        assertThat(order.state()).isEqualTo(OrderState.READY_FOR_HANDOVER);

        // An internal readiness event was generated
        assertThat(order.getDomainEvents()).hasAtLeastOneElementOfType(OrderReadyForHandoverEvent.class);
    }

    @Test
    void shouldRejectScheduleHandoverBeforeReady() {
        // An order whose vehicle is in production (IN_PROGRESS)
        Order order = prepareInProgressOrder();

        // The Salesperson tries to schedule the pickup with the customer for tomorrow
        // The domain blocks this unconditionally - we do not schedule cars that have not been produced
        assertThatThrownBy(() -> order.scheduleHandover(LocalDate.now().plusDays(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order must be in READY_FOR_HANDOVER state to schedule handover");
    }

    @Test
    void shouldRejectReadyForHandoverWhenAlreadyCompleted() {
        // An order that has already been completed and handed over
        Order order = prepareInProgressOrder();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now());
        order.changePaymentStatus(PaymentStatus.PAID);
        order.confirmHandover(); // State: COMPLETED

        // An attempt to mark it again as "ready for handover"
        assertThatThrownBy(order::markAsReadyForHandover)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cannot change state to READY_FOR_HANDOVER. Order is already COMPLETED");
    }
}
