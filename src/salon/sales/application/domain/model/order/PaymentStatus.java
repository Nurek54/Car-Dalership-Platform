package salon.sales.application.domain.model.order;

/** Status opłacenia zamówienia (synchronizowany ze zdarzeń Kontekstu Rozliczeń). */
public enum PaymentStatus {
    UNPAID,   // brak zaksięgowanych wpłat
    PARTIAL,  // wpłata częściowa (np. zadatek)
    PAID      // saldo w pełni pokryte (SettlementCompleted)
}
