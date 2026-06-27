package integration.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.logistics.application.port.in.ReceiveVehicle;
import salon.logistics.infrastructure.in.web.VehicleArrivalRestAdapter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

/** UC-INW-03: Adapter REST przyjęcia pojazdu na plac (zjazd auta z lawety). */
@SpringBootTest(classes = VehicleArrivalRestAdapter.class)
class VehicleArrivalRestAdapterTest {

    @Autowired private VehicleArrivalRestAdapter adapter;
    @MockBean private ReceiveVehicle receiveVehicle;

    @Test
    void shouldDelegateVehicleArrivalToPort() {
        // Pracownik placu rejestruje zjazd auta z lawety (skan VIN)
        adapter.onVehicleArrival(new VehicleArrivalRestAdapter.VehicleArrivalRequest("VIN-1"));

        verify(receiveVehicle).receiveVehicle("VIN-1");
    }

    @Test
    void shouldRejectBlankVin() {
        // Walidacja wejścia — pusty VIN jest odrzucany przed wejściem do domeny
        assertThatThrownBy(() -> adapter.onVehicleArrival(
                new VehicleArrivalRestAdapter.VehicleArrivalRequest("  ")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
