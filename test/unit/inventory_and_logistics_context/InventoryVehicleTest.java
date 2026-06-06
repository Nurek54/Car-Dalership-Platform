package unit.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class InventoryVehicleTest {

    @Test
    void shouldSuccessfullyLockForOrderWhenVehicleIsAvailable() {
        // Arrange (Given)
        InventoryVehicle vehicle = new InventoryVehicle(
                new VinNumber("VIN1234567890ABCDE")
        );
        // Symulujemy przyjęcie auta na plac - status ON_YARD i rola STOCK
        vehicle.receiveOnYard(new ImporterData("MODEL_X", "COLOR_RED"));

        OrderId newOrderId = new OrderId("ORD-100");

        // Act (When)
        vehicle.lockForOrder(newOrderId);

        // Assert (Then)
        // Zgodnie z wymaganiami, auto musi zmienić stan na RESERVED i zostać trwale przypisane do zamówienia
        assertThat(vehicle.getState()).isEqualTo(VehicleState.RESERVED);
        assertThat(vehicle.getLockedForOrder()).isEqualTo(newOrderId);
    }

    @Test
    void shouldThrowExceptionWhenTryingToLockAlreadyReservedVehicle() {
        // Arrange (Given)
        InventoryVehicle vehicle = new InventoryVehicle(new VinNumber("VIN9876543210XYZ"));
        vehicle.receiveOnYard(new ImporterData("MODEL_Y", "COLOR_BLUE"));

        // Pierwsza rezerwacja (np. przez Handlowca A)
        vehicle.lockForOrder(new OrderId("ORD-101"));

        // Act & Assert (When & Then)
        // Druga próba rezerwacji tego samego auta (np. przez Handlowca B) musi natychmiast rzucić błędem
        assertThatThrownBy(() -> vehicle.lockForOrder(new OrderId("ORD-102")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Vehicle is already reserved");
    }

    @Test
    void shouldReleaseReservationAndReturnToStockPool() {
        // Arrange (Given)
        InventoryVehicle vehicle = new InventoryVehicle(new VinNumber("VIN1111111111111"));
        vehicle.lockForOrder(new OrderId("ORD-200")); // Auto zarezerwowane

        // Act (When)
        vehicle.releaseReservation();

        // Assert (Then)
        // W przypadku zerwania kontraktu, auto musi wrócić do wolnej puli na placu
        assertThat(vehicle.getState()).isEqualTo(VehicleState.ON_YARD);
        assertThat(vehicle.getLockedForOrder()).isNull();
    }

    @Test
    void shouldExpirePdiValidityAndBlockHandover() {
        // Arrange (Given)
        InventoryVehicle vehicle = new InventoryVehicle(new VinNumber("VIN2222222222222"));
        vehicle.approvePdi(); // Przegląd PDI pomyślnie zaliczony

        // Act (When)
        // Wywołanie akcji (np. przez cykliczny Cron Job po upływie 90 dni)
        vehicle.expirePdiValidity();

        // Assert (Then)
        // Status PDI zmienia się na EXPIRED, co ostatecznie zablokuje możliwość wydania pojazdu
        assertThat(vehicle.getPdiStatus()).isEqualTo(PdiStatus.EXPIRED);
    }
}