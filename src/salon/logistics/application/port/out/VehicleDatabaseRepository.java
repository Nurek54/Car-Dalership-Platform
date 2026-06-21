package salon.logistics.application.port.out;

import salon.common.model.OrderId;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.VinNumber;

import java.util.List;
import java.util.Optional;

/**
 * OUTBOUND PORT (Figure 37) – "VehicleDatabaseRepository".
 *
 * The local database of vehicles in the yard (Stock). Inventory is the sole owner of the information
 * about which order a given VIN is assigned to and whether it is physically in the yard.
 */
public interface VehicleDatabaseRepository {

    void save(InventoryVehicle vehicle);

    Optional<InventoryVehicle> findByVin(VinNumber vin);

    Optional<InventoryVehicle> findByOrderId(OrderId orderId);

    List<InventoryVehicle> findAll();
}
