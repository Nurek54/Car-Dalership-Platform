package salon.logistics.application.domain.exception;

/**
 * Violation of the state machine of the {@link salon.logistics.application.domain.model.vehicle.InventoryVehicle} aggregate
 * (e.g. an attempt to hand over a vehicle that is not READY_FOR_HANDOVER — UC-INW-06 / A1).
 */
public class IllegalVehicleStateException extends RuntimeException {

    public IllegalVehicleStateException(String message) {
        super(message);
    }
}
