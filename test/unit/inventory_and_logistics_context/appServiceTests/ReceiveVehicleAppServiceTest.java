package unit.inventory_and_logistics_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.common.application.EventPublisher;
import salon.common.model.OrderId;
import salon.logistics.application.domain.event.VehicleDeliveredToStockEvent;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory;
import salon.logistics.application.domain.model.vehicle.SpecificationId;
import salon.logistics.application.domain.model.vehicle.VinNumber;
import salon.logistics.application.port.out.CatalogIntegration;
import salon.logistics.application.port.out.ImporterACL;
import salon.logistics.application.port.out.VehicleDatabaseRepository;
import salon.logistics.application.service.InventoryManagementService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-INW-03: Przyjęcie pojazdu na stan magazynowy. */
@ExtendWith(MockitoExtension.class)
class ReceiveVehicleAppServiceTest {

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
    void shouldReceiveProducedVehicleAndPairWithOrder() { // SCENARIUSZ GŁÓWNY
        // Istnieje pojazd "W produkcji" przypisany do zamówienia ORD-3
        InventoryVehicle inProduction = factory.createForFactoryOrder(
                new VinNumber("VIN-3"), new OrderId("ORD-3"), new SpecificationId("SPEC-3"));
        when(vehicleRepository.findByVin(new VinNumber("VIN-3"))).thenReturn(Optional.of(inProduction));

        service.receiveVehicle("VIN-3");

        // Pojazd zarezerwowany dla klienta, emisja VehicleDeliveredToStock
        verify(vehicleRepository).save(inProduction);
        verify(eventPublisher).publish(any(VehicleDeliveredToStockEvent.class));
    }

    @Test
    void shouldRegisterUnknownVehicleAsFreeStock() { // Scenariusz alternatywny A1
        // Brak pasującego zamówienia — auto przyjęte "na stock"
        when(vehicleRepository.findByVin(new VinNumber("VIN-NEW"))).thenReturn(Optional.empty());

        service.receiveVehicle("VIN-NEW");

        // Pojazd zapisany jako wolny, bez zdarzenia końcowego VehicleDeliveredToStock
        verify(vehicleRepository).save(any(InventoryVehicle.class));
        verify(eventPublisher, never()).publish(any());
    }
}
