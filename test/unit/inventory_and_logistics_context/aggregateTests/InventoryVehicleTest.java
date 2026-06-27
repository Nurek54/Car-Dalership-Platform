package unit.inventory_and_logistics_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.common.model.OrderId;
import salon.logistics.application.domain.exception.IllegalVehicleStateException;
import salon.logistics.application.domain.model.vehicle.ImporterData;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory;
import salon.logistics.application.domain.model.vehicle.SpecificationId;
import salon.logistics.application.domain.model.vehicle.VehicleRole;
import salon.logistics.application.domain.model.vehicle.VehicleState;
import salon.logistics.application.domain.model.vehicle.VinNumber;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** UC-INW-01..06: Agregat InventoryVehicle (maszyna stanów pojazdu w magazynie). */
class InventoryVehicleTest {

    private final InventoryVehicleFactory factory = new InventoryVehicleFactory();

    private InventoryVehicle stockVehicle(String vin) {
        return factory.createStockArrival(
                new ImporterData(new VinNumber(vin), new SpecificationId("SPEC-1"), List.of()));
    }

    @Test
    void shouldReserveOnStockVehicleForOrder() {
        InventoryVehicle vehicle = stockVehicle("VIN-1");

        // Pojazd "Wolny" jest blokowany na konkretne zamówienie
        vehicle.lockForOrder(new OrderId("ORD-1"));

        assertThat(vehicle.state()).isEqualTo(VehicleState.RESERVED);
        assertThat(vehicle.order()).isEqualTo(new OrderId("ORD-1"));
    }

    @Test
    void shouldRejectReservationWhenNotOnStock() {
        InventoryVehicle vehicle = stockVehicle("VIN-2");
        vehicle.lockForOrder(new OrderId("ORD-2")); // już RESERVED

        // Tylko pojazd w stanie ON_STOCK może zostać zarezerwowany
        assertThatThrownBy(() -> vehicle.lockForOrder(new OrderId("ORD-9")))
                .isInstanceOf(IllegalVehicleStateException.class);
    }

    @Test
    void shouldReceiveProducedVehicleOntoYardWhenVinMatches() {
        // Pojazd zamówiony w fabryce jest w produkcji
        InventoryVehicle vehicle = factory.createForFactoryOrder(
                new VinNumber("VIN-3"), new OrderId("ORD-3"), new SpecificationId("SPEC-3"));

        // Skan VIN zgadza się z kartoteką — auto trafia na plac jako zarezerwowane
        vehicle.receiveOnYard(new ImporterData(new VinNumber("VIN-3"), new SpecificationId("SPEC-3"), List.of()));

        assertThat(vehicle.state()).isEqualTo(VehicleState.RESERVED);
    }

    @Test
    void shouldRejectReceiveWhenVinDoesNotMatch() {
        InventoryVehicle vehicle = factory.createForFactoryOrder(
                new VinNumber("VIN-3"), new OrderId("ORD-3"), new SpecificationId("SPEC-3"));

        // Zeskanowany VIN nie pasuje do kartoteki pojazdu
        assertThatThrownBy(() -> vehicle.receiveOnYard(
                new ImporterData(new VinNumber("VIN-INNY"), new SpecificationId("SPEC-3"), List.of())))
                .isInstanceOf(IllegalVehicleStateException.class);
    }

    @Test
    void shouldReleaseReservationBackToStock() {
        InventoryVehicle vehicle = stockVehicle("VIN-4");
        vehicle.lockForOrder(new OrderId("ORD-4"));

        // Zwolnienie blokady cofa pojazd na plac do ponownej sprzedaży
        vehicle.releaseReservation();

        assertThat(vehicle.state()).isEqualTo(VehicleState.ON_STOCK);
        assertThat(vehicle.order()).isNull();
    }

    @Test
    void shouldPrepareReservedVehicleAndHandOver() {
        InventoryVehicle vehicle = stockVehicle("VIN-5");
        vehicle.lockForOrder(new OrderId("ORD-5"));

        // Po rozliczeniu pojazd jest gotowy do wydania, a następnie wydany
        vehicle.prepareForHandover();
        assertThat(vehicle.state()).isEqualTo(VehicleState.READY_FOR_HANDOVER);

        vehicle.handOver();
        assertThat(vehicle.state()).isEqualTo(VehicleState.HANDED_OVER);
    }

    @Test
    void shouldRejectHandOverWhenNotReady() {
        InventoryVehicle vehicle = stockVehicle("VIN-6");
        vehicle.lockForOrder(new OrderId("ORD-6")); // RESERVED, ale nie READY_FOR_HANDOVER

        // Tylko pojazd gotowy do wydania może zostać wydany
        assertThatThrownBy(vehicle::handOver)
                .isInstanceOf(IllegalVehicleStateException.class);
    }

    @Test
    void shouldMarkVehicleAsDemo() {
        InventoryVehicle vehicle = stockVehicle("VIN-7");

        // Pojazd może zostać oznaczony jako egzemplarz demonstracyjny
        vehicle.markAsDemo();

        assertThat(vehicle.role()).isEqualTo(VehicleRole.DEMO);
    }
}
