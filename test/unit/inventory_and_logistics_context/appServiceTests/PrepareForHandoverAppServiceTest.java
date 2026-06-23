package unit.inventory_and_logistics_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.common.application.EventPublisher;
import salon.common.model.OrderId;
import salon.logistics.application.domain.event.VehicleReadyForHandoverEvent;
import salon.logistics.application.domain.exception.VehicleNotFoundException;
import salon.logistics.application.domain.model.vehicle.ImporterData;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory;
import salon.logistics.application.domain.model.vehicle.SpecificationId;
import salon.logistics.application.domain.model.vehicle.VehicleState;
import salon.logistics.application.domain.model.vehicle.VinNumber;
import salon.logistics.application.port.out.CatalogIntegration;
import salon.logistics.application.port.out.ImporterACL;
import salon.logistics.application.port.out.VehicleDatabaseRepository;
import salon.logistics.application.service.InventoryManagementService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-INW-05: Przygotowanie pojazdu do wydania po rozliczeniu. */
@ExtendWith(MockitoExtension.class)
class PrepareForHandoverAppServiceTest {

    @Mock private VehicleDatabaseRepository vehicleRepository;
    @Mock private CatalogIntegration catalogIntegration;
    @Mock private ImporterACL importerAcl;
    @Mock private EventPublisher eventPublisher;

    private InventoryManagementService service;
    private final InventoryVehicleFactory factory = new InventoryVehicleFactory();

    @BeforeEach
    void setUp() {
        service = new InventoryManagementService(vehicleRepository, catalogIntegration, importerAcl, eventPublisher);
    }

    @Test
    void shouldMarkReservedVehicleReadyForHandover() { // SCENARIUSZ GŁÓWNY
        // Zarezerwowany pojazd dla zamówienia ORD-5
        InventoryVehicle reserved = factory.createStockArrival(
                new ImporterData(new VinNumber("VIN-5"), new SpecificationId("SPEC-1"), List.of()));
        reserved.lockForOrder(new OrderId("ORD-5"));
        when(vehicleRepository.findByOrderId(new OrderId("ORD-5"))).thenReturn(Optional.of(reserved));

        service.prepareVehicleForHandover("ORD-5");

        // Status zmieniony na "Gotowy do wydania", emisja VehicleReadyForHandover
        assertThat(reserved.state()).isEqualTo(VehicleState.READY_FOR_HANDOVER);
        verify(vehicleRepository).save(reserved);
        verify(eventPublisher).publish(any(VehicleReadyForHandoverEvent.class));
    }

    @Test
    void shouldThrowWhenVehicleNotFound() {
        // Brak zarezerwowanego pojazdu dla zamówienia
        when(vehicleRepository.findByOrderId(new OrderId("ORD-NONE"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.prepareVehicleForHandover("ORD-NONE"))
                .isInstanceOf(VehicleNotFoundException.class);
    }
}
