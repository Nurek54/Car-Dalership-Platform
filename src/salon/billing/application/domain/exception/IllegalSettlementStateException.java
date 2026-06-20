package salon.billing.application.domain.exception;

/**
 * Proba operacji niedozwolonej w biezacym stanie agregatu (np. wplata na saldo SETTLED,
 * wystawienie dokumentu spoza stanu DRAFT) — naruszenie niezmiennika maszyny stanow.
 */
public class IllegalSettlementStateException extends RuntimeException {

    public IllegalSettlementStateException(String message) {
        super(message);
    }
}
