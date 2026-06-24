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
        // Zamówienie powiązane z ofertą
        Order order = createBaseOrder("ORD-001", "O-100");

        // Klient wybiera finansowanie jako metodę płatności
        order.declarePaymentMethod(PaymentMethod.FINANCING);

        // Metoda płatności jest przypisana, a zdarzenie zarejestrowane w agregacie
        assertThat(order.paymentMethod()).isEqualTo(PaymentMethod.FINANCING);
        assertThat(order.domainEvents())
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
        // Zamówienie było gotowe do wydania, potem umówione i zakończone
        Order order = createBaseOrder("ORD-002", "O-101");

        // Zamówienie musi zostać aktywowane, inaczej domena odrzuci kolejne kroki
        order.activate(); // Stan: IN_PROGRESS

        order.markAsReadyForHandover(); // Stan: READY_FOR_HANDOVER
        order.scheduleHandover(LocalDate.now().plusDays(2)); // Stan: HANDOVER_SCHEDULED
        order.changePaymentStatus(PaymentStatus.PAID);
        order.confirmHandover(); // Stan: COMPLETED

        assertThat(order.state()).isEqualTo(OrderState.COMPLETED);

        // W systemie magazynowym pojawia się błąd
        // System wywołuje metodę kompensującą
        order.revertToReadyForHandover();

        // Zamówienie wraca do poprzedniego, bezpiecznego stanu
        assertThat(order.state()).isEqualTo(OrderState.READY_FOR_HANDOVER);
        assertThat(order.handoverDate()).isNull(); // Data wydania zostaje wyczyszczona
    }

    @Test
    void shouldConfirmHandoverDirectlyFromReadyState() {
        // Zamówienie gotowe do wydania (bez umówionej daty w kalendarzu)
        Order order = createBaseOrder("ORD-003", "O-102");

        order.activate(); // Aktywacja (DRAFT_CREATED -> IN_PROGRESS)
        order.markAsReadyForHandover(); // Stan: READY_FOR_HANDOVER
        order.changePaymentStatus(PaymentStatus.PAID); // Musi być opłacone

        // Klient odbiera samochód od razu na miejscu
        order.confirmHandover();

        // Zamówienie zamyka się poprawnie, z pominięciem HANDOVER_SCHEDULED
        assertThat(order.state()).isEqualTo(OrderState.COMPLETED);
    }
}
