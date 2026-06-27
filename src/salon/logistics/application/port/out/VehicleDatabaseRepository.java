package salon.logistics.application.port.out;

import salon.common.model.OrderId;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.VinNumber;

import java.util.List;
import java.util.Optional;

public interface VehicleDatabaseRepository {

    void save(InventoryVehicle vehicle);

    Optional<InventoryVehicle> findByVin(VinNumber vin);

    Optional<InventoryVehicle> findByOrderId(OrderId orderId);

    List<InventoryVehicle> findAll();
}
