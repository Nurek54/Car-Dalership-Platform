package salon.logistics.infrastructure.out.mock;

import salon.logistics.application.port.out.VehicleDatabaseRepository;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.VehicleRole;
import salon.logistics.application.domain.model.vehicle.VehicleState;
import salon.logistics.application.domain.model.vehicle.VinNumber;
import salon.common.model.OrderId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter wyjściowy (in-memory) portu VehicleDatabaseRepository — do dem i uruchomień offline.
 * W środowisku docelowym zastępuje go InventoryDatabaseAdapter (JPA/PostgreSQL).
 */
public class InMemoryInventoryRepository implements VehicleDatabaseRepository {

    private final Map<VinNumber, InventoryVehicle> store = new HashMap<>();

    @Override
    public void save(InventoryVehicle vehicle) {
        this.store.put(vehicle.getVin(), vehicle);
    }

    @Override
    public Optional<InventoryVehicle> findByVin(VinNumber vin) {
        return Optional.ofNullable(this.store.get(vin));
    }

    @Override
    public Optional<InventoryVehicle> findByOrderId(OrderId orderId) {
        return this.store.values().stream()
                .filter(v -> orderId.equals(v.getOrder()))
                .findFirst();
    }

    @Override
    public List<InventoryVehicle> findAll() {
        return new ArrayList<>(this.store.values());
    }

    /**
     * UC-INW-01, krok 2: wolny pojazd (ON_STOCK, rola STOCK) pasujący do specyfikacji.
     * Mock nie utrzymuje kodów wyposażenia pojazdów — zwraca pierwszy wolny egzemplarz.
     */
    @Override
    public Optional<InventoryVehicle> findAvailableVehicle(List<String> specCodes) {
        return this.store.values().stream()
                .filter(v -> v.getState() == VehicleState.ON_STOCK)
                .filter(v -> v.getRole() == VehicleRole.STOCK)
                .findFirst();
    }
}
