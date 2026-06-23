package integration.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.logistics.application.port.in.ReserveVehicle;
import salon.logistics.infrastructure.in.messaging.FinancingEventSubscriberAdapter;

import static org.mockito.Mockito.verify;

/** UC-INW-01: Adapter nasłuchujący zdarzeń Finansowania — wyzwala rezerwację pojazdu. */
@SpringBootTest(classes = FinancingEventSubscriberAdapter.class)
class FinancingEventSubscriberAdapterTest {

    @Autowired private FinancingEventSubscriberAdapter adapter;
    @MockBean private ReserveVehicle reserveVehicle;

    @Test
    void shouldReserveVehicleOnFinancingApproved() {
        // Zatwierdzone finansowanie inicjuje proces realizacji zamówienia
        adapter.handleFinancingApproved(new FinancingEventSubscriberAdapter.FinancingApproved("ORD-1"));

        verify(reserveVehicle).reserveVehicleForOrder("ORD-1");
    }

    @Test
    void shouldReserveVehicleOnBankTransferDeclared() {
        // Zadeklarowany przelew również inicjuje rezerwację z placu
        adapter.handleBankTransferDeclared(new FinancingEventSubscriberAdapter.BankTransferDeclared("ORD-2"));

        verify(reserveVehicle).reserveVehicleForOrder("ORD-2");
    }
}
