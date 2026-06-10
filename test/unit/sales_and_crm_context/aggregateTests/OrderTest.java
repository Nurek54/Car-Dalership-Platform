package unit.sales_and_crm_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.order.*;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

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

        // Metoda płatności jest przypisana, a zdarzenie jest odłożone w agregacie
        assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.FINANCING);
        assertThat(order.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(FinancingRequestedEvent.class)
                .satisfies(event -> {
                    FinancingRequestedEvent e = (FinancingRequestedEvent) event;
                    assertThat(e.getOrderId()).isEqualTo(new OrderId("ORD-001"));
                });
    }

    @Test
    void shouldExecuteRevertWhenHandoverFails() {
        // Zamówienie było gotowe do wydania, po czym zostało umówione i zrealizowane
        Order order = createBaseOrder("ORD-002", "O-101");

        // Trzeba aktywować zamówienie, inaczej domena odrzuci kolejne kroki
        order.activate(); // Stan: IN_PROGRESS

        order.markAsReadyForHandover(); // Stan: READY_FOR_HANDOVER
        order.scheduleHandover(LocalDate.now().plusDays(2)); // Stan: HANDOVER_SCHEDULED
        order.setPaymentStatus(PaymentStatus.PAID);
        order.confirmHandover(); // Stan: COMPLETED

        assertThat(order.getState()).isEqualTo(OrderState.COMPLETED);

        // Występuje błąd w systemie inwentarza
        // System wywołuje metodę kompensacyjną
        order.revertToReadyForHandover();

        // Zamówienie wraca do poprzedniego, bezpiecznego stanu
        assertThat(order.getState()).isEqualTo(OrderState.READY_FOR_HANDOVER);
        assertThat(order.getHandoverDate()).isNull(); // Data wydania jest czyszczona
    }

    @Test
    void shouldConfirmHandoverDirectlyFromReadyState() {
        // Zamówienie gotowe do wydania (bez umówionej daty w kalendarzu)
        Order order = createBaseOrder("ORD-003", "O-102");

        order.activate(); // Aktywacja (DRAFT -> IN_PROGRESS)
        order.markAsReadyForHandover(); // Stan: READY_FOR_HANDOVER
        order.setPaymentStatus(PaymentStatus.PAID); // Musi być opłacone

        // Klient odbiera auto natychmiast na miejscu
        order.confirmHandover();

        // Zamówienie zamyka się poprawnie omijając HANDOVER_SCHEDULED
        assertThat(order.getState()).isEqualTo(OrderState.COMPLETED);
    }
}