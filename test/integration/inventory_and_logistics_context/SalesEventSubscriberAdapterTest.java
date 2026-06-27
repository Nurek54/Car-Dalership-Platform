package integration.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.logistics.application.port.in.ReleaseVehicle;
import salon.logistics.application.port.out.CatalogIntegration;
import salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter;

import static org.mockito.Mockito.verify;

/** UC-INW-06: Adapter nasłuchujący zdarzeń Sprzedaży — powiązanie zamówienia i komenda wydania. */
@SpringBootTest(classes = SalesEventSubscriberAdapter.class)
class SalesEventSubscriberAdapterTest {

    @Autowired private SalesEventSubscriberAdapter adapter;
    @MockBean private CatalogIntegration catalogIntegration;
    @MockBean private ReleaseVehicle releaseVehicle;

    @Test
    void shouldLinkOrderToSpecificationOnOrderPlaced() {
        // Zdarzenie OrderPlaced wiąże zamówienie ze specyfikacją (event-carried state transfer)
        adapter.handleOrderPlaced(new SalesEventSubscriberAdapter.OrderPlaced("ORD-1", "SPEC-1"));

        verify(catalogIntegration).linkOrderToSpecification("ORD-1", "SPEC-1");
    }

    @Test
    void shouldReleaseVehicleOnReleaseCommand() {
        // Komenda ReleaseVehicle z modułu Sprzedaży zleca fizyczne wydanie
        adapter.handleReleaseVehicle(new SalesEventSubscriberAdapter.ReleaseVehicleCommand("ORD-2"));

        verify(releaseVehicle).releaseVehicle("ORD-2");
    }
}
