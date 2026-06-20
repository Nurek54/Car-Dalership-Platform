package salon.logistics.application.domain.exception;

/**
 * Brak egzemplarza pojazdu spełniającego kryteria (np. brak rezerwacji dla zamówienia).
 */
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(String message) {
        super(message);
    }
}
