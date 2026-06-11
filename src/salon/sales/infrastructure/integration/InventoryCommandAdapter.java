package salon.sales.infrastructure.integration;

import salon.logistics.application.port.in.ReleaseInventoryUseCase;
import salon.logistics.application.port.in.ReserveVehicleUseCase;
import salon.logistics.application.port.out.InventoryRepository;
import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VinNumber;
import salon.sales.application.port.out.InventoryIntegrationPort;

import java.util.Optional;

/**
 * Adapter wyjściowy (in-process) portu {@link InventoryIntegrationPort} — bezpośrednie
 * spięcie z portami wejściowymi Kontekstu Inwentarza (do dem i uruchomień monolitycznych).
 * W środowisku rozproszonym zastępuje go InventoryExternalApiAdapter (HTTP) —
 * kontrakt portu pozostaje bez zmian.
 */
public class InventoryCommandAdapter implements InventoryIntegrationPort {

    private final ReserveVehicleUseCase reserveVehicle;
    private final ReleaseInventoryUseCase releaseInventory;
    private final InventoryRepository inventoryRepository;

    public InventoryCommandAdapter(ReserveVehicleUseCase reserveVehicle,
                                   ReleaseInventoryUseCase releaseInventory,
                                   InventoryRepository inventoryRepository) {
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
