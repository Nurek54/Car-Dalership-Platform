package salon.logistics.application.domain.model.vehicle;

import salon.common.model.OrderId;

/**
 * Fabryka agregatu {@link InventoryVehicle} — jedyny sposób tworzenia egzemplarzy
 * (konstruktor agregatu jest pakietowy). Pilnuje poprawnego stanu początkowego
 * w zależności od ścieżki powstania pojazdu w Inwentarzu.
 */
public class InventoryVehicleFactory {

    /** UC-INW-02: pojazd zlecony w fabryce — powstaje od razu przypisany do zamówienia. */
    public InventoryVehicle createForFactoryOrder(VinNumber vin, OrderId order, SpecificationId specification) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null.");
        }
        return new InventoryVehicle(vin, specification, VehicleRole.STOCK, VehicleState.IN_PRODUCTION, order);
    }

    /** UC-INW-03 / A1: fizyczny pojazd przyjęty na plac bez zamówienia — dostępny jako wolny. */
    public InventoryVehicle createStockArrival(ImporterData data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null.");
        }
        return new InventoryVehicle(data.vin(), data.specificationId(),
                VehicleRole.STOCK, VehicleState.ON_STOCK, null);
    }
}
