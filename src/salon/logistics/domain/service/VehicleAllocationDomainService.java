package salon.logistics.domain.service;

import salon.logistics.domain.model.slot.ProductionSlot;
import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.VehicleRole;
import salon.logistics.domain.model.vehicle.VehicleState;
import salon.shared.model.OrderId;

import java.util.List;

/**
 * Serwis dziedzinowy (UC-INW-07): decyzja Fast Track vs Long Track.
 *
 * Dostaje listę pojazdów (pobraną przez warstwę aplikacji z repozytorium — agregat nie zna bazy)
 * i albo blokuje pasujące auto na placu, albo tworzy slot produkcyjny.
 */
public class VehicleAllocationDomainService {

    /**
     * Fast Track: znajduje wolne auto STOCK na placu i blokuje je. Zwraca true, jeśli sparowano.
     */
    public boolean tryLockExistingVehicle(OrderId orderId,
                                          List<InventoryVehicle> availableVehicles,
                                          List<String> specCodes) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (availableVehicles == null) {
            throw new IllegalArgumentException("availableVehicles must not be null.");
        }
        for (int i = 0; i < availableVehicles.size(); i++) {
            InventoryVehicle vehicle = availableVehicles.get(i);
            if (vehicle.getRole() == VehicleRole.STOCK && vehicle.getState() == VehicleState.ON_STOCK) {
                vehicle.lockForOrder(orderId);
                return true;
            }
        }
        return false;
    }

    /**
     * Long Track: tworzy nowy slot produkcyjny dla zamówienia.
     */
    public ProductionSlot createProductionSlot(OrderId orderId, List<String> specCodes) {
        if (specCodes == null || specCodes.isEmpty()) {
            throw new IllegalArgumentException("specCodes must not be empty.");
        }
        String[] codes = specCodes.toArray(new String[0]);
        return ProductionSlot.createForOrder(orderId, codes);
    }
}
