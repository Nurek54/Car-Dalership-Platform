package unit.inventory_and_logistics_context.domain_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.shared.model.OrderId;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleAllocationDomainServiceTest {

    @Mock
    private InventoryVehicleRepository vehicleRepository;

    @Mock
    private ProductionSlotRepository productionSlotRepository;

    // Wstrzykujemy mocki (udawane repozytoria) do naszej testowanej usługi dziedziny
    @InjectMocks
    private VehicleAllocationDomainService allocationService;

    @Test
    void shouldAllocateExistingVehicleFastTrackWhenMatchIsFoundOnYard() {
        // Arrange (Given)
        OrderId orderId = new OrderId("ORD-999");
        String[] specCodes = {"ENGINE_2.0", "COLOR_RED"};

        InventoryVehicle availableVehicle = new InventoryVehicle(new VinNumber("VIN123"));

        // Uczymy naszego mocka: Kiedy ktoś zapyta o dostępne auto z tą specyfikacją, zwróć availableVehicle
        when(vehicleRepository.findAvailableBySpec(specCodes))
                .thenReturn(Optional.of(availableVehicle));

        // Act (When)
        allocationService.allocateVehicleForOrder(orderId, specCodes);

        // Assert (Then)
        // Sprawdzamy, czy usługa podjęła decyzję Fast Track (zablokowanie istniejącego auta)
        assertThat(availableVehicle.getState()).isEqualTo(VehicleState.RESERVED);
        assertThat(availableVehicle.getLockedForOrder()).isEqualTo(orderId);

        // Upewniamy się, że NIE podjęto decyzji Long Track (nie utworzono slotu produkcyjnego w fabryce)
        verify(productionSlotRepository, never()).save(any(ProductionSlot.class));
    }

    @Test
    void shouldCreateProductionSlotLongTrackWhenNoVehicleIsAvailable() {
        // Arrange (Given)
        OrderId orderId = new OrderId("ORD-888");
        String[] specCodes = {"ENGINE_3.0", "COLOR_BLUE"};

        // Uczymy mocka: Brak takiego auta na placu
        when(vehicleRepository.findAvailableBySpec(specCodes))
                .thenReturn(Optional.empty());

        // Act (When)
        allocationService.allocateVehicleForOrder(orderId, specCodes);

        // Assert (Then)
        // Sprawdzamy, czy usługa podjęła decyzję Long Track: zapisanie nowego slotu produkcyjnego
        verify(productionSlotRepository, times(1)).save(any(ProductionSlot.class));
    }
}