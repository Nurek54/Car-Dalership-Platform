package salon.logistics.application.domain.model.vehicle;

import salon.logistics.application.domain.event.VehicleDeliveredToStockEvent;
import salon.logistics.application.domain.event.VehicleInventoryReleasedEvent;
import salon.logistics.application.domain.event.VehicleReadyForHandoverEvent;
import salon.logistics.application.domain.event.VehicleReservationCancelledEvent;
import salon.logistics.application.domain.event.VehicleReservedFromStockEvent;
import salon.logistics.application.domain.exception.InvalidVehicleStateException;
import salon.common.event.AbstractAggregateRoot;
import salon.common.model.OrderId;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root: Fizyczny Egzemplarz Pojazdu — JEDYNY korzeń agregatu Kontekstu
 * Inwentarza i Logistyki (PDF rozdz. 3.5.4, docs/Inwentarz-Logistyka/Agregaty/inventory-vehicle.md).
 *
 * Reprezentuje cykl życia pojazdu w ekosystemie dealera — od fazy wirtualnej
 * (zamówienie w produkcji) po fazę fizyczną (obecność na placu).
 *
 * Model zgodny 1:1 z diagramem agregatu:
 *   pola:    vin, role, state, order (referencja rozłączna 0..1)
 *   metody:  receiveOnYard, markAsDemo, lockForOrder, releaseReservation
 *            + markReadyForHandover / handOver (UC-INW-05/06 — domknięcie cyklu HANDED_OVER)
 *   stany:   IN_PRODUCTION -> ON_STOCK -> (RESERVED) -> HANDED_OVER
 *
 * Rezerwacja to twarda blokada (mutex): lockForOrder zdejmuje auto z puli sprzedaży
 * i uniemożliwia double-booking; releaseReservation przywraca je do wolnej puli.
 */
public class InventoryVehicle extends AbstractAggregateRoot {

    private final VinNumber vin;
    private VehicleRole role;
    private VehicleState state;
    private OrderId order;                 // null, dopóki nie zarezerwowano (0..1)
    private boolean readyForHandover;      // UC-INW-05: "Gotowy do wydania" (w obrębie RESERVED)

    public InventoryVehicle(VinNumber vin) {
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        this.vin = vin;
        this.role = VehicleRole.STOCK;
        this.state = VehicleState.IN_PRODUCTION;
        this.order = null;
        this.readyForHandover = false;
    }

    /**
     * Wołane przez {@link InventoryVehicleFactory}: wirtualna instancja auta zamówionego
     * w fabryce (UC-INW-02) od początku jest przypisana do zamówienia klienta.
     */
    void assignToOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        this.order = orderId;
    }

    /**
     * UC-INW-03: przyjęcie pojazdu na stan magazynowy (zjazd z lawety, skan VIN).
     * Auto czekające na zamówienie klienta zostaje z nim automatycznie sparowane
     * (status "Zarezerwowany") i emituje VehicleDeliveredToStockEvent. Auto zamówione
     * "na stock" (A1) zostaje wolne (ON_STOCK) — zdarzenie końcowe NIE jest emitowane.
     */
    public void receiveOnYard(ImporterData data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null.");
        }
        if (this.state != VehicleState.IN_PRODUCTION) {
            throw new InvalidVehicleStateException(
                    "Only an IN_PRODUCTION vehicle can be received on yard, was: " + this.state);
        }
        if (this.order != null) {
            this.state = VehicleState.RESERVED;
            registerEvent(new VehicleDeliveredToStockEvent(
                    UUID.randomUUID(), this.order.value(), this.vin.value(), Instant.now()));
        } else {
            this.state = VehicleState.ON_STOCK;
        }
    }

    // Polityka przeznaczenia (VehicleRole): wyłączenie auta z puli sprzedażowej do jazd testowych.
    public void markAsDemo() {
        if (this.state == VehicleState.RESERVED || this.state == VehicleState.HANDED_OVER) {
            throw new InvalidVehicleStateException(
                    "A reserved/handed-over vehicle cannot become DEMO, was: " + this.state);
        }
        this.role = VehicleRole.DEMO;
    }

    /**
     * UC-INW-01: twarda blokada (mutex) pod konkretne zamówienie — przypisanie numeru VIN
     * do zamówienia gwarantuje, że auto nie zostanie sprzedane innemu klientowi.
     */
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
        if (this.role == VehicleRole.DEMO) {
            throw new InvalidVehicleStateException(
                    "A DEMO vehicle is excluded from the sales pool.");
        }
        this.order = orderId;
        this.state = VehicleState.RESERVED;
        registerEvent(new VehicleReservedFromStockEvent(
                UUID.randomUUID(), orderId.value(), this.vin.value(), Instant.now()));
    }

    /**
     * UC-INW-04: zwolnienie blokady (timeout płatności — PaymentDeadlineExpired).
     * Pojazd wraca do wolnej puli, referencja do zamówienia zostaje wyczyszczona.
     */
    public void releaseReservation() {
        if (this.state != VehicleState.RESERVED) {
            throw new InvalidVehicleStateException(
                    "Only a RESERVED vehicle can be released, was: " + this.state);
        }
        String cancelledOrder = this.order.value();
        this.order = null;
        this.readyForHandover = false;
        this.state = VehicleState.ON_STOCK;
        registerEvent(new VehicleReservationCancelledEvent(
                UUID.randomUUID(), cancelledOrder, this.vin.value(), Instant.now()));
    }

    /**
     * UC-INW-05: saldo rozliczone (SettlementCompleted) — auto (wciąż RESERVED) zostaje
     * oznaczone jako "Gotowy do wydania" i ogłasza VehicleReadyForHandoverEvent.
     */
    public void markReadyForHandover() {
        if (this.state != VehicleState.RESERVED) {
            throw new InvalidVehicleStateException(
                    "Only a RESERVED vehicle can be marked ready for handover, was: " + this.state);
        }
        this.readyForHandover = true;
        registerEvent(new VehicleReadyForHandoverEvent(
                UUID.randomUUID(), this.order.value(), this.vin.value(), Instant.now()));
    }

    /**
     * UC-INW-06: zdjęcie pojazdu ze stanu magazynowego (komenda ReleaseVehicle z CRM).
     * Wymaga statusu "Gotowy do wydania" (A1: inny status -> wyjątek, adapter emituje
     * VehicleInventoryReleasedError).
     */
    public void handOver() {
        if (this.state != VehicleState.RESERVED || !this.readyForHandover) {
            throw new InvalidVehicleStateException(
                    "Only a vehicle ready for handover can be handed over, was: " + this.state
                            + " (readyForHandover=" + this.readyForHandover + ")");
        }
        this.state = VehicleState.HANDED_OVER;
        registerEvent(new VehicleInventoryReleasedEvent(
                UUID.randomUUID(), this.order.value(), this.vin.value(), Instant.now()));
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

    public boolean isReadyForHandover() {
        return this.readyForHandover;
    }
}
