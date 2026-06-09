package unit.inventory_and_logistics_context.domain_service;

import org.junit.jupiter.api.Test;
import salon.logistics.domain.model.slot.ProductionSlot;
import salon.logistics.domain.model.slot.SlotState;
import salon.logistics.domain.model.vehicle.ImporterData;
import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VehicleState;
import salon.logistics.domain.model.vehicle.VinNumber;
import salon.logistics.domain.service.VehicleAllocationDomainService;
import salon.shared.model.OrderId;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class VehicleAllocationDomainServiceTest {

    private final VehicleAllocationDomainService allocationService = new VehicleAllocationDomainService();

    private InventoryVehicle onStock(String vin) {
        InventoryVehicle vehicle = new InventoryVehicle(new VinNumber(vin));
        vehicle.receiveOnYard(new ImporterData(vin));
        return vehicle;
    }

    @Test
    void shouldAllocateExistingVehicleFastTrackWhenMatchIsFoundOnYard() {
        OrderId orderId = new OrderId("ORD-999");
        InventoryVehicle available = onStock("VIN123FASTTRACK");

        boolean locked = allocationService.tryLockExistingVehicle(
                orderId, List.of(available), List.of("ENGINE_2.0", "COLOR_RED"));

        // Decyzja Fast Track: istniejące auto zostaje zablokowane
        assertThat(locked).isTrue();
        assertThat(available.getState()).isEqualTo(VehicleState.RESERVED);
        assertThat(available.getOrder()).isEqualTo(orderId);
    }

    @Test
    void shouldNotLockAndAllowLongTrackWhenNoVehicleIsAvailable() {
        OrderId orderId = new OrderId("ORD-888");

        // Brak aut na placu -> Fast Track niemożliwy
        boolean locked = allocationService.tryLockExistingVehicle(
                orderId, List.of(), List.of("ENGINE_3.0", "COLOR_BLUE"));
        assertThat(locked).isFalse();

        // Decyzja Long Track: utworzenie slotu produkcyjnego
        ProductionSlot slot = allocationService.createProductionSlot(
                orderId, List.of("ENGINE_3.0", "COLOR_BLUE"));
        assertThat(slot.getOrderId()).isEqualTo(orderId);
        assertThat(slot.getState()).isEqualTo(SlotState.SCHEDULED);
    }
}
