package salon.logistics.infrastructure.out.mock;

import salon.common.model.OrderId;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.VinNumber;
import salon.logistics.application.port.out.VehicleDatabaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ADAPTER WYJŚCIOWY (Rysunek 37: DBAdapter) – implementacja {@link VehicleDatabaseRepository}
 * w pamięci. Lokalna baza pojazdów na placu; klucz naturalny = VIN.
 */
public class InMemoryInventoryRepository implements VehicleDatabaseRepository {

    private final Map<String, InventoryVehicle> byVin = new ConcurrentHashMap<>();

    @Override
    public void save(InventoryVehicle vehicle) {
        this.byVin.put(vehicle.getVin().value(), vehicle);
    }

    @Override
    public Optional<InventoryVehicle> findByVin(VinNumber vin) {
        return Optional.ofNullable(this.byVin.get(vin.value()));
    }

    @Override
    public Optional<InventoryVehicle> findByOrderId(OrderId orderId) {
        return this.byVin.values().stream()
                .filter(v -> v.getOrder() != null && v.getOrder().value().equals(orderId.value()))
                .findFirst();
    }

    @Override
    public List<InventoryVehicle> findAll() {
        return new ArrayList<>(this.byVin.values());
    }
}
