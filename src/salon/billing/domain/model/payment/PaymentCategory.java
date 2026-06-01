package salon.billing.domain.model.payment;

// Kategoria wpłaty (UC-ROZ-01, krok 3).
public enum PaymentCategory {
    DEPOSIT,        // Zadatek
    ADVANCE,        // Zaliczka
    FINAL_PAYMENT   // Płatność końcowa (ustawiana w innych przepływach)
}
