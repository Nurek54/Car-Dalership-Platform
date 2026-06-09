package unit.inventory_and_logistics_context.events;

import org.junit.jupiter.api.Test;
import salon.logistics.domain.event.VehicleReceivedOnYardEvent;
import salon.logistics.domain.model.vehicle.ImporterData;
import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VinNumber;

import static org.assertj.core.api.Assertions.*;

class InventoryAndLogisticsEventTest {

    @Test
    void shouldEmitVehicleReceivedOnYardEventWhenCarArrives() {
        InventoryVehicle vehicle = new InventoryVehicle(new VinNumber("VIN12345ONYARD"));

        vehicle.receiveOnYard(new ImporterData("VIN12345ONYARD"));

        assertThat(vehicle.getDomainEvents())
                .hasAtLeastOneElementOfType(VehicleReceivedOnYardEvent.class);
    }
}
