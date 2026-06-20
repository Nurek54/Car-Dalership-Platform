package salon.logistics.application.domain.exception;

/**
 * Naruszenie maszyny stanów agregatu {@link salon.logistics.application.domain.model.vehicle.InventoryVehicle}
 * (np. próba wydania pojazdu, który nie jest READY_FOR_HANDOVER — UC-INW-06 / A1).
 */
public class IllegalVehicleStateException extends RuntimeException {

    public IllegalVehicleStateException(String message) {
        super(message);
    }
}
