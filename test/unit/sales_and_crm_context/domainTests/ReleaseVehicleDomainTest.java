package unit.sales_and_crm_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.sales.domain.model.order.*;
import salon.sales.domain.model.offer.OfferId;
import salon.shared.model.Money;
import salon.shared.model.OrderId;
import salon.sales.domain.event.OrderCompletedEvent;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/** UC-CRM-05: Rejestracja fizycznego wydania pojazdu */
class ReleaseVehicleDomainTest {

    // Metoda pomocnicza
    private Order prepareScheduledOrder() {
        Order order = new Order(new OrderId("ORD-999"), new OfferId("OFF-999"), Money.of(150000, "PLN"));
        order.activate();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now().plusDays(1)); // Stan: HANDOVER_SCHEDULED
        order.pullDomainEvents();
        return order;
    }

    @Test
    void shouldConfirmHandoverAndCompleteOrder() { // SCENARIUSZ GŁÓWNY
        // Przekazanie umówione
        Order order = prepareScheduledOrder();

        // Użytkownik potwierdza fizyczne wydanie kluczyków klientowi
        order.confirmHandover();

        // Agregat przechodzi do końcowego, niemutowalnego stanu COMPLETED
        assertThat(order.getState()).isEqualTo(OrderState.COMPLETED);
        // Generuje się zdarzenie zamykające
        assertThat(order.getDomainEvents()).hasAtLeastOneElementOfType(OrderCompletedEvent.class);
    }

    @Test
    void shouldRevertToReadyForHandoverWhenInventoryReleaseFails() {

        Order order = prepareScheduledOrder();
        order.confirmHandover(); // Stan ostateczny: COMPLETED
        order.pullDomainEvents();

        // Moduł CRM odbiera z modułu Inwentarza zdarzenie błędu (VehicleInventoryReleasedError),
        order.revertToReadyForHandover();

        assertThat(order.getState()).isEqualTo(OrderState.READY_FOR_HANDOVER);

        assertThat(order.getHandoverDate()).isNull();
    }
}