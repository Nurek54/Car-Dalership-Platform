package salon.logistics.infrastructure.mock;

import salon.logistics.application.port.out.VehicleRepository;
import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VinNumber;
import salon.shared.model.OrderId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryVehicleRepository implements VehicleRepository {

    private final Map<VinNumber, InventoryVehicle> store = new HashMap<>();

    // UC-INW-03: zamówienia czekające na dostawę konkretnego VIN-u (rejestrowane przy alokacji Long Track).
    private final Map<VinNumber, OrderId> pendingOrders = new HashMap<>();

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

    @Override
    public Optional<InventoryVehicle> findByOrderId(OrderId orderId) {
        return this.store.values().stream()
                .filter(v -> orderId.equals(v.getOrder()))
                .findFirst();
    }

    @Override
    public Optional<OrderId> findPendingOrderForSpec(VinNumber vin) {
        return Optional.ofNullable(this.pendingOrders.get(vin));
    }

    /** Pomocnicze (demo/testy): powiązanie przyszłej dostawy VIN-u z oczekującym zamówieniem. */
    public void registerPendingOrder(VinNumber vin, OrderId orderId) {
        this.pendingOrders.put(vin, orderId);
    }
}
