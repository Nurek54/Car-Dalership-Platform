package salon.logistics.domain.model.vehicle;

import salon.logistics.domain.event.VehicleReceivedOnYardEvent;
import salon.logistics.domain.exceptions.InvalidVehicleStateException;
import salon.shared.event.AbstractAggregateRoot;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root: fizyczny egzemplarz pojazdu — "cyfrowy bliźniak" auta (inventory-vehicle.md).
 *
 * Model zgodny 1:1 z diagramem agregatu:
 *   pola:    vin, role, state, order (referencja rozłączna do zamówienia, 0..1)
 *   metody:  receiveOnYard, markAsDemo, lockForOrder, releaseReservation
 *   stany:   IN_PRODUCTION -> ON_STOCK -> (RESERVED) -> HANDED_OVER
 *
 * Rezerwacja to mutex: lockForOrder zdejmuje auto z puli sprzedaży, releaseReservation je zwraca.
 */
public class InventoryVehicle extends AbstractAggregateRoot {

    private final VinNumber vin;
    private VehicleRole role;
    private VehicleState state;
    private OrderId order; // null, dopóki nie zarezerwowano (referencja rozłączna 0..1)

    public InventoryVehicle(VinNumber vin) {
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        this.vin = vin;
        this.role = VehicleRole.STOCK;
        this.state = VehicleState.IN_PRODUCTION;
        this.order = null;
    }

    // UC-INW-01: przyjęcie na plac po weryfikacji tożsamości przez ACL Importera.
    public void receiveOnYard(ImporterData data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null.");
        }
        if (this.state != VehicleState.IN_PRODUCTION) {
            throw new InvalidVehicleStateException(
                    "Only an IN_PRODUCTION vehicle can be received on yard, was: " + this.state);
        }
        this.state = VehicleState.ON_STOCK;
        registerEvent(new VehicleReceivedOnYardEvent(UUID.randomUUID(), this.vin.value(), Instant.now()));
    }

    // UC-INW-03: nadanie roli auta demonstracyjnego.
    public void markAsDemo() {
        if (this.state == VehicleState.RESERVED || this.state == VehicleState.HANDED_OVER) {
            throw new InvalidVehicleStateException(
                    "A reserved/handed-over vehicle cannot become DEMO, was: " + this.state);
        }
        this.role = VehicleRole.DEMO;
    }

    // UC-INW-07: twarda blokada (mutex) pod konkretne zamówienie.
    public void lockForOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (this.state == VehicleState.RESERVED) {
            throw new IllegalStateException("Vehicle is already reserved for order: " + this.order);
        }
        if (this.state != VehicleState.ON_STOCK) {
            throw new InvalidVehicleStateException(
                    "Only an ON_STOCK vehicle can be locked for an order, was: " + this.state);
        }
        this.order = orderId;
        this.state = VehicleState.RESERVED;
    }

    // UC-INW-06: zwolnienie rezerwacji po anulowaniu zamówienia.
    public void releaseReservation() {
        if (this.state != VehicleState.RESERVED) {
            throw new InvalidVehicleStateException(
                    "Only a RESERVED vehicle can be released, was: " + this.state);
        }
        this.order = null;
        this.state = VehicleState.ON_STOCK;
    }

    public VinNumber getVin() {
        return this.vin;
    }

    public VehicleRole getRole() {
        return this.role;
    }

    public VehicleState getState() {
        return this.state;
    }

    public OrderId getOrder() {
        return this.order;
    }
}
