package unit.inventory_and_logistics_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.common.application.EventPublisher;
import salon.logistics.application.domain.event.VehicleIsNotOnStockEvent;
import salon.logistics.application.domain.event.VehicleReservedFromStockEvent;
import salon.logistics.application.domain.model.vehicle.ImporterData;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory;
import salon.logistics.application.domain.model.vehicle.SpecificationId;
import salon.logistics.application.domain.model.vehicle.VinNumber;
import salon.logistics.application.port.out.CatalogIntegration;
import salon.logistics.application.port.out.ImporterACL;
import salon.logistics.application.port.out.VehicleDatabaseRepository;
import salon.logistics.application.service.InventoryManagementService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-INW-01: Weryfikacja dostępności i rezerwacja pojazdu z placu. */
@ExtendWith(MockitoExtension.class)
class ReserveVehicleAppServiceTest {

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
    void shouldReserveMatchingStockVehicle() { // SCENARIUSZ GŁÓWNY
        // Zamówienie wskazuje na specyfikację SPEC-1
        when(catalogIntegration.findSpecificationByOrder("ORD-1")).thenReturn(Optional.of("SPEC-1"));
        InventoryVehicle free = factory.createStockArrival(
                new ImporterData(new VinNumber("VIN-1"), new SpecificationId("SPEC-1"), List.of()));
        when(vehicleRepository.findAll()).thenReturn(List.of(free));

        service.reserveVehicleForOrder("ORD-1");

        // Pasujący pojazd jest blokowany i zapisywany, a kontekst emituje VehicleReservedFromStock
        verify(vehicleRepository).save(free);
        verify(eventPublisher).publish(any(VehicleReservedFromStockEvent.class));
    }

    @Test
    void shouldEmitNotOnStockWhenNoMatchingVehicle() { // Scenariusz alternatywny A1
        // Brak wolnego pojazdu o wymaganej specyfikacji na placu
        when(catalogIntegration.findSpecificationByOrder("ORD-2")).thenReturn(Optional.of("SPEC-2"));
        when(vehicleRepository.findAll()).thenReturn(List.of());

        service.reserveVehicleForOrder("ORD-2");

        // Rezerwacja wstrzymana — emisja VehicleNotOnStock, brak zapisu
        verify(vehicleRepository, never()).save(any());
        verify(eventPublisher).publish(any(VehicleIsNotOnStockEvent.class));
    }
}
