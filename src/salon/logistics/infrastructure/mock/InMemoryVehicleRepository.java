package salon.logistics.infrastructure.mock;

import salon.logistics.application.port.out.VehicleRepository;
import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VinNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryVehicleRepository implements VehicleRepository {

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
    public List<InventoryVehicle> findAll() {
        return new ArrayList<>(this.store.values());
    }
}
