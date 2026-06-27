package unit.inventory_and_logistics_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.common.application.EventPublisher;
import salon.logistics.application.domain.event.FactoryOrderFailedEvent;
import salon.logistics.application.domain.event.FactoryOrderPlacedEvent;
import salon.logistics.application.domain.exception.FactoryOrderFailedException;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.port.out.CatalogIntegration;
import salon.logistics.application.port.out.ImporterACL;
import salon.logistics.application.port.out.VehicleDatabaseRepository;
import salon.logistics.application.service.InventoryManagementService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** UC-INW-02: Zlecenie produkcji pojazdu w fabryce. */
@ExtendWith(MockitoExtension.class)
class OrderFromFactoryAppServiceTest {

    @Mock private VehicleDatabaseRepository vehicleRepository;
    @Mock private CatalogIntegration catalogIntegration;
    @Mock private ImporterACL importerAcl;
    @Mock private EventPublisher eventPublisher;

    private InventoryManagementService service;

    @BeforeEach
    void setUp() {
        service = new InventoryManagementService(vehicleRepository, catalogIntegration, importerAcl, eventPublisher);
    }

    @Test
    void shouldPlaceFactoryOrderAndEmitEvent() { // SCENARIUSZ GŁÓWNY
        // Zamówienie ma specyfikację z kodami opcji; fabryka przyjmuje zlecenie i nadaje VIN
        when(catalogIntegration.findSpecificationByOrder("ORD-1")).thenReturn(Optional.of("SPEC-1"));
        when(catalogIntegration.findOptionCodes("SPEC-1")).thenReturn(List.of("B2", "C1"));
        when(importerAcl.placeFactoryOrder(eq("ORD-1"), anyList())).thenReturn("VIN-123");

        service.orderVehicleFromFactory("ORD-1");

        // Pojazd "W produkcji" zapisany, emisja FactoryOrderPlaced
        verify(vehicleRepository).save(any(InventoryVehicle.class));
        verify(eventPublisher).publish(any(FactoryOrderPlacedEvent.class));
    }

    @Test
    void shouldEmitFailedEventWhenFactoryRejects() { // Scenariusz alternatywny A1
        // API fabryki zwraca błąd przy próbie złożenia zlecenia
        when(catalogIntegration.findSpecificationByOrder("ORD-2")).thenReturn(Optional.of("SPEC-2"));
        when(catalogIntegration.findOptionCodes("SPEC-2")).thenReturn(List.of("B2"));
        when(importerAcl.placeFactoryOrder(eq("ORD-2"), anyList()))
                .thenThrow(new FactoryOrderFailedException("Problem z połączeniem z fabryką"));

        service.orderVehicleFromFactory("ORD-2");

        // Emisja FactoryOrderFailed, brak zapisu pojazdu
        verify(eventPublisher).publish(any(FactoryOrderFailedEvent.class));
        verify(vehicleRepository, never()).save(any());
    }
}
