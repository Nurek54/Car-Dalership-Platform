package unit.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import salon.logistics.domain.model.vehicle.ImporterData;
import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VehicleRole;
import salon.logistics.domain.model.vehicle.VehicleState;
import salon.logistics.domain.model.vehicle.VinNumber;
import salon.shared.model.OrderId;

import static org.assertj.core.api.Assertions.*;

class InventoryVehicleTest {

    private InventoryVehicle receivedVehicle(String vin) {
        InventoryVehicle vehicle = new InventoryVehicle(new VinNumber(vin));
        vehicle.receiveOnYard(new ImporterData(vin)); // IN_PRODUCTION -> ON_STOCK
        return vehicle;
    }

    @Test
    void shouldEnterStockWhenReceivedOnYard() {
        InventoryVehicle vehicle = new InventoryVehicle(new VinNumber("VIN1234567890ABCDE"));

        vehicle.receiveOnYard(new ImporterData("VIN1234567890ABCDE"));

        assertThat(vehicle.getState()).isEqualTo(VehicleState.ON_STOCK);
        assertThat(vehicle.getRole()).isEqualTo(VehicleRole.STOCK);
        assertThat(vehicle.getOrder()).isNull();
    }

    @Test
    void shouldSuccessfullyLockForOrderWhenVehicleIsOnStock() {
        InventoryVehicle vehicle = receivedVehicle("VIN1234567890ABCDE");
        OrderId newOrderId = new OrderId("ORD-100");

        vehicle.lockForOrder(newOrderId);

        // Auto musi zmienić stan na RESERVED i zostać trwale przypisane do zamówienia
        assertThat(vehicle.getState()).isEqualTo(VehicleState.RESERVED);
        assertThat(vehicle.getOrder()).isEqualTo(newOrderId);
    }

    @Test
    void shouldThrowExceptionWhenTryingToLockAlreadyReservedVehicle() {
        InventoryVehicle vehicle = receivedVehicle("VIN9876543210XYZAB");
        vehicle.lockForOrder(new OrderId("ORD-101"));

        // Druga próba rezerwacji tego samego auta musi natychmiast rzucić błędem
        assertThatThrownBy(() -> vehicle.lockForOrder(new OrderId("ORD-102")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Vehicle is already reserved");
    }

    @Test
    void shouldReleaseReservationAndReturnToStockPool() {
        InventoryVehicle vehicle = receivedVehicle("VIN1111111111111AA");
        vehicle.lockForOrder(new OrderId("ORD-200"));

        vehicle.releaseReservation();

        // Po zerwaniu kontraktu auto wraca do wolnej puli na placu
        assertThat(vehicle.getState()).isEqualTo(VehicleState.ON_STOCK);
        assertThat(vehicle.getOrder()).isNull();
    }

    @Test
    void shouldMarkVehicleAsDemo() {
        InventoryVehicle vehicle = receivedVehicle("VIN2222222222222BB");

        vehicle.markAsDemo();

        assertThat(vehicle.getRole()).isEqualTo(VehicleRole.DEMO);
    }
}
