package unit.sales_and_crm_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.sales.application.domain.event.FinancingRequestedEvent;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.order.*;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class OrderTest {

    private Order createBaseOrder(String rawOrderId, String rawOfferId) {
        return new Order(
                new OrderId(rawOrderId),
                new OfferId(rawOfferId),
                Money.of(150000, "PLN")
        );
    }

    @Test
    void shouldDeclarePaymentMethodAndRegisterDomainEvent() {
        // An order linked to the offer
        Order order = createBaseOrder("ORD-001", "O-100");

        // The customer chooses financing as the payment method
        order.declarePaymentMethod(PaymentMethod.FINANCING);

        // The payment method is assigned, and the event is recorded in the aggregate
        assertThat(order.paymentMethod()).isEqualTo(PaymentMethod.FINANCING);
        assertThat(order.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(FinancingRequestedEvent.class)
                .satisfies(event -> {
                    FinancingRequestedEvent e = (FinancingRequestedEvent) event;
                    assertThat(e.orderId()).isEqualTo("ORD-001");
                });
    }

    @Test
    void shouldExecuteRevertWhenHandoverFails() {
        // The order was ready for handover, then was scheduled and completed
        Order order = createBaseOrder("ORD-002", "O-101");

        // The order must be activated, otherwise the domain will reject the next steps
        order.activate(); // State: IN_PROGRESS

        order.markAsReadyForHandover(); // State: READY_FOR_HANDOVER
        order.scheduleHandover(LocalDate.now().plusDays(2)); // State: HANDOVER_SCHEDULED
        order.changePaymentStatus(PaymentStatus.PAID);
        order.confirmHandover(); // State: COMPLETED

        assertThat(order.state()).isEqualTo(OrderState.COMPLETED);

        // An error occurs in the inventory system
        // The system invokes the compensating method
        order.revertToReadyForHandover();

        // The order returns to the previous, safe state
        assertThat(order.state()).isEqualTo(OrderState.READY_FOR_HANDOVER);
        assertThat(order.handoverDate()).isNull(); // The handover date is cleared
    }

    @Test
    void shouldConfirmHandoverDirectlyFromReadyState() {
        // An order ready for handover (without a scheduled date in the calendar)
        Order order = createBaseOrder("ORD-003", "O-102");

        order.activate(); // Aktywacja (DRAFT_CREATED -> IN_PROGRESS)
        order.markAsReadyForHandover(); // State: READY_FOR_HANDOVER
        order.changePaymentStatus(PaymentStatus.PAID); // It must be paid

        // The customer picks up the car immediately on the spot
        order.confirmHandover();

        // The order closes correctly, bypassing HANDOVER_SCHEDULED
        assertThat(order.state()).isEqualTo(OrderState.COMPLETED);
    }
}
