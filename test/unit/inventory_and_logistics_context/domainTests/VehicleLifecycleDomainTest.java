package unit.inventory_and_logistics_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.common.model.OrderId;
import salon.logistics.application.domain.model.vehicle.ImporterData;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory;
import salon.logistics.application.domain.model.vehicle.SpecificationId;
import salon.logistics.application.domain.model.vehicle.VehicleState;
import salon.logistics.application.domain.model.vehicle.VinNumber;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** UC-INW-02..06: Pełny cykl życia pojazdu w magazynie (na agregacie). */
class VehicleLifecycleDomainTest {

    private final InventoryVehicleFactory factory = new InventoryVehicleFactory();

    @Test
    void factoryRouteShouldEndHandedOver() {
        // Trasa fabryczna: produkcja -> przyjęcie na plac -> gotowość -> wydanie
        InventoryVehicle vehicle = factory.createForFactoryOrder(
                new VinNumber("VIN-1"), new OrderId("ORD-1"), new SpecificationId("SPEC-1"));
        assertThat(vehicle.state()).isEqualTo(VehicleState.IN_PRODUCTION);

        vehicle.receiveOnYard(new ImporterData(new VinNumber("VIN-1"), new SpecificationId("SPEC-1"), List.of()));
        assertThat(vehicle.state()).isEqualTo(VehicleState.RESERVED);

        vehicle.prepareForHandover();
        assertThat(vehicle.state()).isEqualTo(VehicleState.READY_FOR_HANDOVER);

        vehicle.handOver();
        assertThat(vehicle.state()).isEqualTo(VehicleState.HANDED_OVER);
    }

    @Test
    void stockRouteShouldEndHandedOver() {
        // Trasa magazynowa: wolny pojazd na placu -> rezerwacja -> gotowość -> wydanie
        InventoryVehicle vehicle = factory.createStockArrival(
                new ImporterData(new VinNumber("VIN-2"), new SpecificationId("SPEC-2"), List.of()));
        assertThat(vehicle.state()).isEqualTo(VehicleState.ON_STOCK);

        vehicle.lockForOrder(new OrderId("ORD-2"));
        vehicle.prepareForHandover();
        vehicle.handOver();

        assertThat(vehicle.state()).isEqualTo(VehicleState.HANDED_OVER);
    }

    @Test
    void releaseReservationShouldReturnVehicleToStock() {
        // UC-INW-04: zwolnienie blokady cofa pojazd na plac
        InventoryVehicle vehicle = factory.createStockArrival(
                new ImporterData(new VinNumber("VIN-3"), new SpecificationId("SPEC-3"), List.of()));
        vehicle.lockForOrder(new OrderId("ORD-3"));

        vehicle.releaseReservation();

        assertThat(vehicle.state()).isEqualTo(VehicleState.ON_STOCK);
        assertThat(vehicle.order()).isNull();
    }
}
