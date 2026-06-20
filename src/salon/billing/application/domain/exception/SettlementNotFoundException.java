package salon.billing.application.domain.exception;

/**
 * Brak otwartego salda (Settlement) dla wskazanego zamowienia — np. wplata lub zadanie faktury
 * dla zamowienia, dla ktorego nie zainicjowano jeszcze rozliczenia.
 */
public class SettlementNotFoundException extends RuntimeException {

    public SettlementNotFoundException(String message) {
        super(message);
    }
}
