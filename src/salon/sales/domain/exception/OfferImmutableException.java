package salon.sales.domain.exception;

/**
 * Oferta w stanie terminalnym (ACCEPTED/REJECTED) jest niemutowalna — to zamknięty
 * rozdział, historyczny dowód wynegocjowanych warunków (PDF rozdz. 3.3.4).
 */
public class OfferImmutableException extends InvalidOfferStateException {
    public OfferImmutableException(String message) {
        super(message);
    }
}
