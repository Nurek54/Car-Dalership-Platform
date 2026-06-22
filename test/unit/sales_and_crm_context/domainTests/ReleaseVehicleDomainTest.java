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
        // Wydanie jest umówione
        Order order = prepareScheduledOrder();

        // Użytkownik potwierdza fizyczne przekazanie kluczyków klientowi
        order.confirmHandover();

        // Agregat przechodzi w finalny, niemutowalny stan COMPLETED
        assertThat(order.state()).isEqualTo(OrderState.COMPLETED);
        // Generowane jest zdarzenie zamykające
        assertThat(order.getDomainEvents()).hasAtLeastOneElementOfType(OrderCompletedEvent.class);
    }

    @Test
    void shouldRevertToReadyForHandoverWhenInventoryReleaseFails() {

        Order order = prepareScheduledOrder();
        order.confirmHandover(); // Stan końcowy: COMPLETED
        order.pullDomainEvents();

        // Moduł CRM otrzymuje zdarzenie błędu z modułu Magazynu (VehicleInventoryReleasedError),
        order.revertToReadyForHandover();

        assertThat(order.state()).isEqualTo(OrderState.READY_FOR_HANDOVER);

        assertThat(order.handoverDate()).isNull();
    }
}
