package unit.inventory_and_logistics_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.common.application.EventPublisher;
import salon.common.model.OrderId;
import salon.logistics.application.domain.event.VehicleInventoryReleasedErrorEvent;
import salon.logistics.application.domain.event.VehicleInventoryReleasedEvent;
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

/** UC-INW-06: Zdjęcie pojazdu ze stanu magazynowego (fizyczne wydanie). */
@ExtendWith(MockitoExtension.class)
class ReleaseVehicleAppServiceTest {

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

    private InventoryVehicle vehicleInState(String vin, String orderId, boolean ready) {
        InventoryVehicle vehicle = factory.createStockArrival(
                new ImporterData(new VinNumber(vin), new SpecificationId("SPEC-1"), List.of()));
        vehicle.lockForOrder(new OrderId(orderId));
        if (ready) {
            vehicle.prepareForHandover();
        }
        return vehicle;
    }

    @Test
    void shouldHandOverReadyVehicle() { // SCENARIUSZ GŁÓWNY
        // Pojazd "Gotowy do wydania" dla zamówienia ORD-6
        InventoryVehicle ready = vehicleInState("VIN-6", "ORD-6", true);
        when(vehicleRepository.findByOrderId(new OrderId("ORD-6"))).thenReturn(Optional.of(ready));

        service.releaseVehicle("ORD-6");

        // Status "Wydany", emisja VehicleInventoryReleased
        assertThat(ready.state()).isEqualTo(VehicleState.HANDED_OVER);
        verify(vehicleRepository).save(ready);
        verify(eventPublisher).publish(any(VehicleInventoryReleasedEvent.class));
    }

    @Test
    void shouldEmitErrorWhenVehicleNotReady() { // Scenariusz alternatywny A1
        // Pojazd ma status inny niż "Gotowy do wydania" (tu: RESERVED)
        InventoryVehicle reserved = vehicleInState("VIN-7", "ORD-7", false);
        when(vehicleRepository.findByOrderId(new OrderId("ORD-7"))).thenReturn(Optional.of(reserved));

        service.releaseVehicle("ORD-7");

        // Komenda odrzucona — emisja VehicleInventoryReleasedError, brak zapisu wydania
        verify(eventPublisher).publish(any(VehicleInventoryReleasedErrorEvent.class));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenVehicleNotFound() {
        // Brak pojazdu do wydania dla zamówienia
        when(vehicleRepository.findByOrderId(new OrderId("ORD-NONE"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.releaseVehicle("ORD-NONE"))
                .isInstanceOf(VehicleNotFoundException.class);
    }
}
