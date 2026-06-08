package salon.logistics.domain.exceptions;

/**
 * Wyjątek dziedzinowy: operacja niedozwolona w bieżącym stanie pojazdu
 * (np. rezerwacja auta już zarezerwowanego, wydanie bez ważnego PDI).
 */
public class InvalidVehicleStateException extends RuntimeException {
    public InvalidVehicleStateException(String message) {
        super(message);
    }
}
