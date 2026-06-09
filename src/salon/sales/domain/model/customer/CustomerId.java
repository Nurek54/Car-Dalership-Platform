package salon.sales.domain.model.customer;

/**
 * Value Object: identyfikator Klienta w kontekście Sprzedaży i CRM.
 *
 * Wspólny dla agregatów {@code Customer} i {@code Offer} (patrz
 * docs/Architecture/SalesArchitecture.md oraz docs/Agregate/Sales/customer-offer-order.md),
 * dlatego mieszka w pakiecie {@code model.customer}, a nie wewnątrz pakietu oferty.
 *
 * To odrębny typ od identyfikatora klienta w kontekście Finansowania
 * (salon.financing.domain.model.financing.CustomerId) — każdy Bounded Context
 * ma własny model (model rozłączny).
 */
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId must not be blank.");
        }
    }
}
