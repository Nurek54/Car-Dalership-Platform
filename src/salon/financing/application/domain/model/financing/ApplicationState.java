package salon.financing.application.domain.model.financing;

/**
 * Stan wniosku o finansowanie (Rysunek 43).
 *
 * DRAFT – wniosek utworzony lokalnie z danych zamówienia;
 * PENDING – wysłany do banku, „W trakcie weryfikacji bankowej" (UC-FIN-01);
 * APPROVED – bank wydał decyzję pozytywną (UC-FIN-02);
 * REJECTED – bank wydał decyzję negatywną (UC-FIN-02 / A1).
 */
public enum ApplicationState {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED
}
