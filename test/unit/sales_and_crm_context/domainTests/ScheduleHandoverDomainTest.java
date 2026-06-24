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

/** UC-CRM-04: Obsługa zaproszenia klienta po odbiór */
class ScheduleHandoverDomainTest {

    // Metoda pomocnicza - symuluje zamówienie będące w produkcji (IN_PROGRESS)
    private Order prepareInProgressOrder() {
        Order order = new Order(new OrderId("ORD-10"), new OfferId("OFF-10"), Money.of(150000, "PLN"));
        order.activate(); // Przejście z DRAFT_CREATED do IN_PROGRESS
        order.pullDomainEvents(); // Czyścimy listę zdarzeń przed właściwym testem
        return order;
    }

    @Test
    void shouldBecomeReadyForHandoverAndEmitEvent() { // SCENARIUSZ GŁÓWNY
        // Zamówienie jest w toku (fabryka wyprodukowała samochód)
        Order order = prepareInProgressOrder();

        // System logistyczny zgłasza, że samochód zjechał z lawety i jest na placu
        order.markAsReadyForHandover();

        // Stan zamówienia pozwala na wydanie (READY_FOR_HANDOVER)
        assertThat(order.state()).isEqualTo(OrderState.READY_FOR_HANDOVER);

        // Wygenerowano wewnętrzne zdarzenie gotowości
        assertThat(order.domainEvents()).hasAtLeastOneElementOfType(OrderReadyForHandoverEvent.class);
    }

    @Test
    void shouldRejectScheduleHandoverBeforeReady() {
        // Zamówienie, którego pojazd jest w produkcji (IN_PROGRESS)
        Order order = prepareInProgressOrder();

        // Sprzedawca próbuje umówić odbiór z klientem na jutro
        // Domena blokuje to bezwarunkowo - nie umawiamy aut, które nie zostały wyprodukowane
        assertThatThrownBy(() -> order.scheduleHandover(LocalDate.now().plusDays(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order must be in READY_FOR_HANDOVER state to schedule handover");
    }

    @Test
    void shouldRejectReadyForHandoverWhenAlreadyCompleted() {
        // Zamówienie, które już zostało zakończone i wydane
        Order order = prepareInProgressOrder();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now());
        order.changePaymentStatus(PaymentStatus.PAID);
        order.confirmHandover(); // Stan: COMPLETED

        // Próba ponownego oznaczenia jako „gotowe do wydania"
        assertThatThrownBy(order::markAsReadyForHandover)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cannot change state to READY_FOR_HANDOVER. Order is already COMPLETED");
    }
}
