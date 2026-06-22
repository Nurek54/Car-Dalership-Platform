package salon.logistics.application.domain.exception;

public class IllegalVehicleStateException extends RuntimeException {

    public IllegalVehicleStateException(String message) {
        super(message);
    }
}
