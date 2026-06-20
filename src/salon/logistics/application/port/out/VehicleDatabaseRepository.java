package salon.logistics.application.port.out;

import salon.common.model.OrderId;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.VinNumber;

import java.util.List;
import java.util.Optional;

/**
 * PORT WYJŚCIOWY (Rysunek 37) – „VehicleDatabaseRepository”.
 *
 * Lokalna baza pojazdów na placu (Stock). Inwentarz jest jedynym właścicielem informacji
 * o tym, do jakiego zamówienia przypisany jest dany VIN i czy fizycznie jest on na placu.
 */
public interface VehicleDatabaseRepository {

    void save(InventoryVehicle vehicle);

    Optional<InventoryVehicle> findByVin(VinNumber vin);

    Optional<InventoryVehicle> findByOrderId(OrderId orderId);

    List<InventoryVehicle> findAll();
}
