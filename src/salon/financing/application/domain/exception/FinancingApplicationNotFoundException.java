package salon.financing.application.domain.exception;

/**
 * Brak wniosku o finansowanie dla wskazanego zamówienia (np. decyzja banku dla nieznanego wniosku).
 */
public class FinancingApplicationNotFoundException extends RuntimeException {

    public FinancingApplicationNotFoundException(String message) {
        super(message);
    }
}
