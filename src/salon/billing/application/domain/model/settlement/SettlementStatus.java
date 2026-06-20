package salon.billing.application.domain.model.settlement;

/**
 * Typ standardowy (Enumeration na diagramie klas) — stan salda zamówienia.
 *
 * OPEN            – saldo otwarte, brak (pełnych) wpłat;
 * PARTIAL_PAYMENT – zaksięgowano wpłatę częściową (UC-FIR-03 / A1);
 * SETTLED         – saldo = 0, zamówienie w pełni opłacone (UC-FIR-03).
 */
public enum SettlementStatus {
    OPEN,
    PARTIAL_PAYMENT,
    SETTLED
}
