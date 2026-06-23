package unit.inventory_and_logistics_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.common.model.OrderId;
import salon.logistics.application.domain.exception.IllegalVehicleStateException;
import salon.logistics.application.domain.model.vehicle.ImporterData;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory;
import salon.logistics.application.domain.model.vehicle.SpecificationId;
import salon.logistics.application.domain.model.vehicle.VinNumber;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** UC-INW-04/06: Reguły stanu przy zwalnianiu i wydaniu pojazdu (na agregacie). */
class VehicleReleaseDomainTest {

    private final InventoryVehicleFactory factory = new InventoryVehicleFactory();

    private InventoryVehicle onStock(String vin) {
        return factory.createStockArrival(
                new ImporterData(new VinNumber(vin), new SpecificationId("SPEC-1"), List.of()));
    }

    @Test
    void shouldRejectHandOverWhenNotReadyForHandover() { // UC-INW-06 / A1
        InventoryVehicle vehicle = onStock("VIN-1");
        vehicle.lockForOrder(new OrderId("ORD-1")); // tylko RESERVED, nie READY_FOR_HANDOVER

        // Niewłaściwy status pojazdu — wydanie jest blokowane przez agregat
        assertThatThrownBy(vehicle::handOver)
                .isInstanceOf(IllegalVehicleStateException.class);
    }

    @Test
    void shouldRejectReservationReleaseWhenNotReserved() {
        InventoryVehicle vehicle = onStock("VIN-2"); // ON_STOCK, brak rezerwacji

        // Tylko zarezerwowany pojazd można zwolnić z blokady
        assertThatThrownBy(vehicle::releaseReservation)
                .isInstanceOf(IllegalVehicleStateException.class);
    }
}
