package unit.sales_and_crm_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.sales.domain.model.order.*;
import salon.sales.domain.model.offer.OfferId;
import salon.shared.model.Money;
import salon.shared.model.OrderId;
import salon.sales.domain.event.OrderReadyForHandoverEvent;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/** UC-CRM-04: Obsługa zaproszenia klienta po odbiór */
class ScheduleHandoverDomainTest {

    // Metoda pomocnicza - symuluje zamówienie, które jest w trakcie produkcji (IN_PROGRESS)
    private Order prepareInProgressOrder() {
        Order order = new Order(new OrderId("ORD-10"), new OfferId("OFF-10"), Money.of(150000, "PLN"));
        order.activate(); // Przejście z DRAFT_CREATED do IN_PROGRESS
        order.pullDomainEvents(); // Czyścimy listę zdarzeń przed właściwym testem
        return order;
    }

    @Test
    void shouldBecomeReadyForHandoverAndEmitEvent() { // SCENARIUSZ GŁÓWNY
        // Zamówienie jest w trakcie realizacji (fabryka wyprodukowała auto)
        Order order = prepareInProgressOrder();

        // System logistyczny informuje, że auto zjechało z lawety i jest na placu
        order.markAsReadyForHandover();

        // Stan zamówienia pozwala na wydanie (READY_FOR_HANDOVER)
        assertThat(order.getState()).isEqualTo(OrderState.READY_FOR_HANDOVER);

        // Wygenerowano wewnętrzne zdarzenie o gotowości
        assertThat(order.getDomainEvents()).hasAtLeastOneElementOfType(OrderReadyForHandoverEvent.class);
    }

    @Test
    void shouldRejectScheduleHandoverBeforeReady() {
        // Zamówienie, którego pojazd jest w produkcji (IN_PROGRESS)
        Order order = prepareInProgressOrder();

        // Handlowiec próbuje umówić odbiór z klientem na jutro
        // Domena bezwzględnie to blokuje - nie umawiamy aut, których nie ma wyprodukowanych
        assertThatThrownBy(() -> order.scheduleHandover(LocalDate.now().plusDays(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order must be in READY_FOR_HANDOVER state to schedule handover");
    }

    @Test
    void shouldRejectReadyForHandoverWhenAlreadyCompleted() {
        // Zamówienie, które zostało już zakończone i wydane
        Order order = prepareInProgressOrder();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now());
        order.setPaymentStatus(PaymentStatus.PAID);
        order.confirmHandover(); // Stan: COMPLETED

        // Próba ponownego oznaczenia go jako "gotowe do wydania"
        assertThatThrownBy(order::markAsReadyForHandover)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cannot change state to READY_FOR_HANDOVER. Order is already COMPLETED");
    }
}