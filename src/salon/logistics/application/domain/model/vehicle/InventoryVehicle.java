package salon.logistics.application.domain.model.vehicle;

import salon.common.model.OrderId;
import salon.logistics.application.domain.exception.IllegalVehicleStateException;

public class InventoryVehicle {

    private final VinNumber vin;
    private final SpecificationId specification; 
    private VehicleRole role;
    private VehicleState state;
    private OrderId order; 

    
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

    
    public void releaseReservation() {
        if (this.state != VehicleState.RESERVED) {
            throw new IllegalVehicleStateException(
                    "Only a RESERVED vehicle's reservation can be released (current: " + this.state + ").");
        }
        this.order = null;
        this.state = VehicleState.ON_STOCK;
    }

    
    public void prepareForHandover() {
        if (this.state != VehicleState.RESERVED) {
            throw new IllegalVehicleStateException(
                    "Only a RESERVED vehicle can be prepared for handover (current: " + this.state + ").");
        }
        this.state = VehicleState.READY_FOR_HANDOVER;
    }

    
    public void handOver() {
        if (this.state != VehicleState.READY_FOR_HANDOVER) {
            throw new IllegalVehicleStateException(
                    "Only a READY_FOR_HANDOVER vehicle can be handed over (current: " + this.state + ").");
        }
        this.state = VehicleState.HANDED_OVER;
    }

    
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

    
    public OrderId order() {
        return order;
    }
}
