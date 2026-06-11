package salon.logistics.application.port.out;

import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VinNumber;
import salon.shared.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * Port wyjściowy: repozytorium agregatu InventoryVehicle —
 * węzeł "InventoryRepository" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 */
public interface InventoryRepository {

    void save(InventoryVehicle vehicle);

    Optional<InventoryVehicle> findByVin(VinNumber vin);

    Optional<InventoryVehicle> findByOrderId(OrderId orderId);

    List<InventoryVehicle> findAll();

    /**
     * UC-INW-01, krok 2: wyszukanie wolnego pojazdu (status ON_STOCK, rola STOCK)
     * pasującego do specyfikacji z zamówienia.
     */
    Optional<InventoryVehicle> findAvailableVehicle(List<String> specCodes);
}
