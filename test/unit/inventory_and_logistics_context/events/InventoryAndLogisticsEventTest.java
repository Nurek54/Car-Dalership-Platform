package unit.inventory_and_logistics_context.events;

import org.junit.jupiter.api.Test;

class InventoryAndLogisticsEventTest {

    @Test
    void shouldEmitVehicleReceivedInYardEventWhenCarArrives() {
        // Arrange
        InventoryVehicle vehicle = new InventoryVehicle(new VinNumber("VIN12345"));
        ImporterData data = new ImporterData("MODEL_X", "BLACK");

        // Act
        vehicle.receiveOnYard(data);

        // Assert - Sprawdzamy czy agregat wyemitował zdarzenie
        assertThat(vehicle.getDomainEvents())
                .hasAtLeastOneElementOfType(VehicleReceivedInYardEvent.class);
    }

    @Test
    void shouldEmitVehicleReadyForHandoverEventWhenPdiIsApproved() {
        // Arrange
        InventoryVehicle vehicle = new InventoryVehicle(new VinNumber("VIN12345"));
        vehicle.receiveOnYard(new ImporterData("MODEL_X", "BLACK"));

        // Act
        vehicle.approvePdi();

        // Assert
        assertThat(vehicle.getDomainEvents())
                .hasAtLeastOneElementOfType(VehicleReadyForHandoverEvent.class);
    }
}