package salon.billing.application.domain.exception;

/** Brak rozliczenia dla wskazanego zamówienia w kontekście Fakturowania (UC-FIR-01..03). */
public class SettlementNotFoundException extends RuntimeException {
    public SettlementNotFoundException(String message) {
        super(message);
    }
}
