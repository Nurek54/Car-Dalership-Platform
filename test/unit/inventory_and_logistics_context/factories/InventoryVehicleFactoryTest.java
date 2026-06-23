package unit.inventory_and_logistics_context.factories;

import org.junit.jupiter.api.Test;
import salon.common.model.OrderId;
import salon.logistics.application.domain.model.vehicle.ImporterData;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory;
import salon.logistics.application.domain.model.vehicle.SpecificationId;
import salon.logistics.application.domain.model.vehicle.VehicleRole;
import salon.logistics.application.domain.model.vehicle.VehicleState;
import salon.logistics.application.domain.model.vehicle.VinNumber;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Fabryka agregatu InventoryVehicle — jedyne ścieżki tworzenia pojazdu w magazynie. */
class InventoryVehicleFactoryTest {

    private final InventoryVehicleFactory factory = new InventoryVehicleFactory();

    @Test
    void shouldCreateVehicleForFactoryOrderInProduction() {
        // UC-INW-02: pojazd zlecony do fabryki startuje jako "W produkcji"
        InventoryVehicle vehicle = factory.createForFactoryOrder(
                new VinNumber("VIN-1"), new OrderId("ORD-1"), new SpecificationId("SPEC-1"));

        assertThat(vehicle.state()).isEqualTo(VehicleState.IN_PRODUCTION);
        assertThat(vehicle.role()).isEqualTo(VehicleRole.STOCK);
        assertThat(vehicle.order()).isEqualTo(new OrderId("ORD-1"));
        assertThat(vehicle.vin()).isEqualTo(new VinNumber("VIN-1"));
    }

    @Test
    void shouldCreateStockArrivalAsFreeVehicle() {
        // UC-INW-03 / A1: auto przyjęte "na stock" jest wolne (ON_STOCK), bez zamówienia
        InventoryVehicle vehicle = factory.createStockArrival(
                new ImporterData(new VinNumber("VIN-2"), new SpecificationId("SPEC-2"), List.of()));

        assertThat(vehicle.state()).isEqualTo(VehicleState.ON_STOCK);
        assertThat(vehicle.role()).isEqualTo(VehicleRole.STOCK);
        assertThat(vehicle.order()).isNull();
    }

    @Test
    void shouldRejectNullArguments() {
        // Niezmienniki tworzenia: zamówienie i dane przyjęcia są wymagane
        assertThatThrownBy(() -> factory.createForFactoryOrder(
                new VinNumber("VIN-3"), null, new SpecificationId("SPEC-3")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.createStockArrival(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
