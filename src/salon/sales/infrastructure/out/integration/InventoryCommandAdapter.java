package salon.sales.infrastructure.out.integration;

import salon.logistics.application.port.in.ReleaseVehicle;
import salon.logistics.application.port.in.ReserveVehicle;
import salon.logistics.application.port.out.VehicleDatabaseRepository;
import salon.logistics.application.domain.model.vehicle.InventoryVehicle;
import salon.logistics.application.domain.model.vehicle.VinNumber;
import salon.sales.application.port.out.InventoryIntegration;

import java.util.Optional;

/**
 * Adapter wyjściowy (in-process) portu {@link InventoryIntegration} — bezpośrednie
 * spięcie z portami wejściowymi Kontekstu Inwentarza (do dem i uruchomień monolitycznych).
 * W środowisku rozproszonym zastępuje go InventoryExternalApiAdapter (HTTP) —
 * kontrakt portu pozostaje bez zmian.
 */
public class InventoryCommandAdapter implements InventoryIntegration {

    private final ReserveVehicle reserveVehicle;
    private final ReleaseVehicle releaseInventory;
    private final VehicleDatabaseRepository inventoryRepository;

    public InventoryCommandAdapter(ReserveVehicle reserveVehicle,
                                   ReleaseVehicle releaseInventory,
                                   VehicleDatabaseRepository inventoryRepository) {
        if (reserveVehicle == null) {
            throw new IllegalArgumentException("reserveVehicle must not be null.");
        }
        if (releaseInventory == null) {
            throw new IllegalArgumentException("releaseInventory must not be null.");
        }
        if (inventoryRepository == null) {
            throw new IllegalArgumentException("inventoryRepository must not be null.");
        }
        this.reserveVehicle = reserveVehicle;
        this.releaseInventory = releaseInventory;
        this.inventoryRepository = inventoryRepository;
    }

    /** UC-CRM-03 -> UC-INW-01/02: rezerwacja pojazdu z placu lub zlecenie produkcji. */
    @Override
    public void allocateVehicleOrProductionSlot(String orderId) {
        this.reserveVehicle.reserveVehicleForOrder(orderId);
    }

    /** UC-CRM-05 -> UC-INW-06: komenda ReleaseVehicle (po numerze VIN wydawanego auta). */
    @Override
    public void releasePhysicalVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.isBlank()) {
            return; // pojazd nie został jeszcze przypisany — nie ma czego zwalniać
        }
        Optional<InventoryVehicle> vehicle =
                this.inventoryRepository.findByVin(new VinNumber(vehicleId));
        if (vehicle.isEmpty() || vehicle.get().getOrder() == null) {
            System.out.println("[InventoryCommandAdapter] Brak rezerwacji dla VIN "
                    + vehicleId + " — komenda ReleaseVehicle pominięta.");
            return;
        }
        this.releaseInventory.releaseVehicle(vehicle.get().getOrder().value());
    }
}
