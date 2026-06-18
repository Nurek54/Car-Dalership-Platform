package salon.sales.application.domain.model.order;

/**
 * Zadeklarowana przez klienta forma płatności za pojazd (UC-CRM-03, krok 4) —
 * zgodnie z docs/Agregate/Sales/customer-offer-order.md.
 *
 * Od deklaracji zależy, które zdarzenie opuści kontekst Sprzedaży:
 * BankTransferDeclaredEvent (przelew) lub FinancingRequestedEvent (finansowanie).
 */
public enum PaymentMethod {
    BANK_TRANSFER,  // przelew własny klienta
    FINANCING       // kredyt/leasing przez Kontekst Finansowania
}
