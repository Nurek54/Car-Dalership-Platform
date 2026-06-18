package salon.logistics.application.domain.model.vehicle;

import salon.common.model.OrderId;

/**
 * Fabryka agregatu InventoryVehicle — węzeł "InventoryVehicleFactory"
 * w docs/Inwentarz-Logistyka/LogisticsArchitecture.md (PDF rozdz. 3.5.3).
 *
 * UC-INW-02: nowo zamówione, lecz jeszcze niewyprodukowane auto jest reprezentowane
 * jako wirtualna instancja w początkowym stanie IN_PRODUCTION, od razu przypisana
 * do zamówienia klienta.
 * UC-INW-03 (A1): auto, które przyjechało bez wcześniejszej kartoteki (zamówione
 * "na stock"), dostaje nową kartotekę w stanie IN_PRODUCTION — przyjęcie na plac
 * realizuje metoda receiveOnYard agregatu.
 */
public class InventoryVehicleFactory {

    /** UC-INW-02: wirtualne auto zamówione w fabryce pod konkretne zamówienie klienta. */
    public InventoryVehicle createOrderedFromFactory(VinNumber vin, OrderId orderId) {
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        InventoryVehicle vehicle = new InventoryVehicle(vin);
        vehicle.assignToOrder(orderId);
        return vehicle;
    }

    /** UC-INW-03 (A1): nowa kartoteka pojazdu bez przypisanego zamówienia ("na stock"). */
    public InventoryVehicle createUnassigned(VinNumber vin) {
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        return new InventoryVehicle(vin);
    }
}
