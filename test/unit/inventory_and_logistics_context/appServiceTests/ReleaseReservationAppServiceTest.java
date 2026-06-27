package unit.inventory_and_logistics_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.common.application.EventPublisher;
import salon.common.model.OrderId;
import salon.logistics.application.domain.event.VehicleReservationCancelledEvent;
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

/** UC-INW-04: Zwolnienie blokady pojazdu (po upływie terminu płatności). */
@ExtendWith(MockitoExtension.class)
class ReleaseReservationAppServiceTest {

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

    private InventoryVehicle reservedVehicle(String vin, String orderId) {
        InventoryVehicle vehicle = factory.createStockArrival(
                new ImporterData(new VinNumber(vin), new SpecificationId("SPEC-1"), List.of()));
        vehicle.lockForOrder(new OrderId(orderId));
        return vehicle;
    }

    @Test
    void shouldReleaseReservationBackToStock() { // SCENARIUSZ GŁÓWNY
        // Zablokowany pojazd dla zamówienia ORD-4
        InventoryVehicle reserved = reservedVehicle("VIN-4", "ORD-4");
        when(vehicleRepository.findByOrderId(new OrderId("ORD-4"))).thenReturn(Optional.of(reserved));

        service.releaseReservation("ORD-4");

        // Pojazd wraca na plac, emisja VehicleReservationCancelled
        verify(vehicleRepository).save(reserved);
        verify(eventPublisher).publish(any(VehicleReservationCancelledEvent.class));
    }

    @Test
    void shouldDoNothingWhenNoReservationExists() { // Scenariusz alternatywny A1
        // Zablokowany pojazd został wcześniej usunięty/wydany
        when(vehicleRepository.findByOrderId(new OrderId("ORD-X"))).thenReturn(Optional.empty());

        service.releaseReservation("ORD-X");

        // Brak operacji — nic nie zapisujemy ani nie emitujemy
        verify(vehicleRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
}
