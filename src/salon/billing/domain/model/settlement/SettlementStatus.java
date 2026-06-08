package salon.billing.domain.model.settlement;

/**
 * Status rozliczenia. Agregat sam decyduje o przejściu na podstawie wyliczonego salda.
 */
public enum SettlementStatus {
    OPEN,             // brak zaksięgowanych wpłat
    PARTIAL_PAYMENT,  // zaksięgowano część należności
    SETTLED           // należność pokryta w całości
}
