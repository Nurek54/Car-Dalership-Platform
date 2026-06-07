package integration.driven_adapters.database_adapter.logistics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.logistics.domain.model.InventoryVehicle;
import salon.logistics.domain.model.VinNumber;
import salon.logistics.domain.model.VehicleRole;
import salon.logistics.domain.model.VehicleState;
import salon.logistics.domain.model.PdiStatus;
import salon.shared.model.OrderId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(InventoryVehicleDatabaseAdapter.class)
class InventoryVehicleDatabaseAdapterTest {

    @Autowired
    private InventoryVehicleDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: Pełny stan fizycznego pojazdu i Enumy
    @Test
    void shouldSaveAndRetrieveInventoryVehicleWithAllEnumsMapped() {
        // Arrange - Tworzymy pojazd po unikalnym numerze VIN
        VinNumber vin = new VinNumber("VIN1234567890ABCDE");
        InventoryVehicle vehicle = new InventoryVehicle(vin);

        // Symulujemy zablokowanie auta dla konkretnego zamówienia
        OrderId orderId = new OrderId("ORD-777");
        vehicle.lockForOrder(orderId);

        // Act - Adapter mapuje i zapisuje obiekt do bazy H2
        adapter.save(vehicle);

        // Odczyt z bazy (często w logistyce wyszukuje się po numerze VIN, a nie wewnątrz-systemowym ID)
        Optional<InventoryVehicle> retrievedVehicle = adapter.findByVin(vin);

        // Assert
        assertThat(retrievedVehicle).isPresent();
        InventoryVehicle retrieved = retrievedVehicle.get();

        // Weryfikacja tożsamości i referencji do zamówienia
        assertThat(retrieved.getVin()).isEqualTo(vin);
        assertThat(retrieved.getLockedForOrder()).isEqualTo(orderId);

        // Weryfikacja mapowania Enumów - lockForOrder() powinno zmienić stan na RESERVED
        assertThat(retrieved.getState()).isEqualTo(VehicleState.RESERVED);

        // Weryfikacja statusów początkowych
        assertThat(retrieved.getPdiStatus()).isEqualTo(PdiStatus.PENDING);
        assertThat(retrieved.getRole()).isEqualTo(VehicleRole.STOCK);
    }

    // 2. BRAK DANYCH: Wyszukiwanie auta, które nie zjechało na plac
    @Test
    void shouldReturnEmptyOptionalWhenVehicleWithGivenVinDoesNotExist() {
        // Act
        Optional<InventoryVehicle> result = adapter.findByVin(new VinNumber("VIN-UNKNOWN"));

        // Assert
        assertThat(result).isEmpty();
    }
}