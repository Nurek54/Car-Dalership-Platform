package integration.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salon.common.model.OrderId;
import salon.logistics.application.domain.model.vehicle.ImporterData;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory;
import salon.logistics.application.domain.model.vehicle.SpecificationId;
import salon.logistics.application.domain.model.vehicle.VinNumber;
import salon.logistics.application.port.out.VehicleDatabaseRepository;
import salon.logistics.infrastructure.out.mock.InMemoryInventoryRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Integracja adaptera persystencji inwentarza (zapis/odczyt po VIN i po zamówieniu). */
@SpringBootTest(classes = InMemoryInventoryRepository.class)
class InMemoryInventoryRepositoryTest {

    @Autowired private VehicleDatabaseRepository repository;
    private final InventoryVehicleFactory factory = new InventoryVehicleFactory();

    private InventoryVehicle stock(String vin) {
        return factory.createStockArrival(
                new ImporterData(new VinNumber(vin), new SpecificationId("SPEC-1"), List.of()));
    }

    @Test
    void shouldSaveAndFindByVin() {
        InventoryVehicle vehicle = stock("VIN-1");

        // Zapis i odczyt pojazdu po numerze VIN
        repository.save(vehicle);

        assertThat(repository.findByVin(new VinNumber("VIN-1"))).contains(vehicle);
        assertThat(repository.findByVin(new VinNumber("VIN-NONE"))).isEmpty();
    }

    @Test
    void shouldFindReservedVehicleByOrderId() {
        InventoryVehicle vehicle = stock("VIN-2");
        vehicle.lockForOrder(new OrderId("ORD-2"));
        repository.save(vehicle);

        // Zarezerwowany pojazd jest odnajdywany po identyfikatorze zamówienia
        assertThat(repository.findByOrderId(new OrderId("ORD-2"))).contains(vehicle);
    }

    @Test
    void shouldReturnAllVehicles() {
        repository.save(stock("VIN-3"));
        repository.save(stock("VIN-4"));

        assertThat(repository.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }
}
