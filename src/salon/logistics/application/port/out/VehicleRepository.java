package salon.logistics.application.port.out;

import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VinNumber;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository {
    void save(InventoryVehicle vehicle);
    Optional<InventoryVehicle> findByVin(VinNumber vin);
    List<InventoryVehicle> findAll();
}
