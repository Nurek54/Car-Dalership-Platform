package salon.logistics.application.domain.exception;

/**
 * No vehicle instance matching the criteria (e.g. no reservation for the order).
 */
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(String message) {
        super(message);
    }
}
