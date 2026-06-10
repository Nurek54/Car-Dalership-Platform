package salon.logistics.application.port.out;

import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VinNumber;
import salon.shared.model.OrderId;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository {
    void save(InventoryVehicle vehicle);
    Optional<InventoryVehicle> findByVin(VinNumber vin);
    List<InventoryVehicle> findAll();

    /** UC-INW-04/05: pojazd aktualnie zarezerwowany pod dane zamówienie. */
    Optional<InventoryVehicle> findByOrderId(OrderId orderId);

    /** UC-INW-03: oczekujące zamówienie czekające na dostawę auta o danej specyfikacji/VIN. */
    Optional<OrderId> findPendingOrderForSpec(VinNumber vin);
}
