package salon.logistics.application.domain.model.vehicle;

import salon.common.model.OrderId;
import salon.logistics.application.domain.exception.IllegalVehicleStateException;

/**
 * AGGREGATE ROOT (Figure 38) – a physical vehicle instance in Inventory.
 *
 * The aggregate guards the invariants of the vehicle's life cycle (state machine) and is the only
 * place where the status and the link to the order change. The reference to the order is
 * DISJOINT (through {@link OrderId} from the shared kernel) — Inventory does not know the Order aggregate.
 *
 * The business logic (state transitions) is here; orchestration and events — in the application service.
 */
public class InventoryVehicle {

    private final VinNumber vin;
    private final SpecificationId specification; // may be null for "stock" cars without a specification
    private VehicleRole role;
    private VehicleState state;
    private OrderId order; // 0..1 — assigned only after reservation/ordering

    /** Package-private constructor — instances are created only by {@link InventoryVehicleFactory}. */
    InventoryVehicle(VinNumber vin, SpecificationId specification,
                     VehicleRole role, VehicleState state, OrderId order) {
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        if (role == null) {
            throw new IllegalArgumentException("role must not be null.");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must not be null.");
        }
        this.vin = vin;
        this.specification = specification;
        this.role = role;
        this.state = state;
        this.order = order;
    }

    /** UC-INW-01, steps 3–4: hard lock of a free vehicle from the yard for the order. */
    public void lockForOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (this.state != VehicleState.ON_STOCK) {
            throw new IllegalVehicleStateException(
                    "Only an ON_STOCK vehicle can be reserved (current: " + this.state + ").");
        }
        this.order = orderId;
        this.state = VehicleState.RESERVED;
    }

    /**
     * UC-INW-03 (Figure 38: {@code receiveOnYard(ImporterData)}): the transporter unloading of a vehicle
     * ordered from the factory. The importer data (VIN scan) is reconciled with the record, after which
     * the vehicle is matched with the order (RESERVED).
     */
    public void receiveOnYard(ImporterData data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null.");
        }
        if (!this.vin.equals(data.vin())) {
            throw new IllegalVehicleStateException(
                    "The scanned VIN " + data.vin() + " does not match the record of vehicle " + this.vin + ".");
        }
        if (this.state != VehicleState.IN_PRODUCTION) {
            throw new IllegalVehicleStateException(
                    "Only an IN_PRODUCTION vehicle can be received into the yard (with matching to an order) (current: "
                            + this.state + ").");
        }
        this.state = VehicleState.RESERVED;
    }

    /** UC-INW-04, step 3: automatic removal of the lock — the vehicle returns to the yard as free. */
    public void releaseReservation() {
        if (this.state != VehicleState.RESERVED) {
            throw new IllegalVehicleStateException(
                    "Only a RESERVED vehicle's reservation can be released (current: " + this.state + ").");
        }
        this.order = null;
        this.state = VehicleState.ON_STOCK;
    }

    /**
     * UC-INW-05, step 3: after the balance is settled the vehicle becomes ready for handover.
     * (A method outside the minimal set from Figure 38 — required by UC-INW-05.)
     */
    public void prepareForHandover() {
        if (this.state != VehicleState.RESERVED) {
            throw new IllegalVehicleStateException(
                    "Only a RESERVED vehicle can be prepared for handover (current: " + this.state + ").");
        }
        this.state = VehicleState.READY_FOR_HANDOVER;
    }

    /**
     * UC-INW-06, step 3: physical handover — removal from the active stock.
     * (A method outside the minimal set from Figure 38 — required by UC-INW-06.)
     */
    public void handOver() {
        if (this.state != VehicleState.READY_FOR_HANDOVER) {
            throw new IllegalVehicleStateException(
                    "Only a READY_FOR_HANDOVER vehicle can be handed over (current: " + this.state + ").");
        }
        this.state = VehicleState.HANDED_OVER;
    }

    /** Marking the instance as a demonstration one (Figure 38). */
    public void markAsDemo() {
        this.role = VehicleRole.DEMO;
    }

    public VinNumber vin() {
        return vin;
    }

    public SpecificationId specification() {
        return specification;
    }

    public VehicleRole role() {
        return role;
    }

    public VehicleState state() {
        return state;
    }

    /** May return null when the vehicle is not assigned to any order (disjoint model). */
    public OrderId order() {
        return order;
    }
}
