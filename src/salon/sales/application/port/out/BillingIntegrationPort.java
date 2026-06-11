package salon.sales.application.port.out;

import salon.shared.model.Money;

/**
 * Port wyjściowy (driven) do Kontekstu Fakturowania i Rozliczeń.
 * Komunikacja zgodna z kanwami: prośba o proformę/zadatek, rozliczenie anulowanego
 * zamówienia oraz domknięcie salda po wydaniu pojazdu.
 */
public interface BillingIntegrationPort {

    /** UC-FIR-01/02: zlecenie wystawienia dokumentu proforma na zadeklarowaną kwotę. */
    void requestProformaInvoice(String orderId, Money amount);

    /** Rozliczenie zadatku po anulowaniu zamówienia (z powodem rezygnacji). */
    void processCancelledOrderBilling(String orderId, String reason);

    /** UC-CRM-05: domknięcie salda końcowego po fizycznym wydaniu pojazdu. */
    void closeOrderBalance(String orderId);
}
