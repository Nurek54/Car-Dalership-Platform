package salon.financing.domain.service;

import salon.shared.model.Money;

import java.math.BigDecimal;

/**
 * Serwis dziedzinowy (UC-FIN-02): estymacja wartości rezydualnej pojazdu na potrzeby polisy GAP.
 * Odciąża agregat z logiki matematycznej.
 *
 * ZALOZENIE: prosty model liniowej deprecjacji (procent rocznie) — do potwierdzenia testem.
 */
public class ResidualValueCalculationService {

    // Roczna utrata wartości (np. 15%).
    private static final BigDecimal ANNUAL_DEPRECIATION = new BigDecimal("0.15");

    public Money calculateResidualValue(Money basePrice, int ageInYears) {
        if (basePrice == null) {
            throw new IllegalArgumentException("basePrice must not be null.");
        }
        if (ageInYears < 0) {
            throw new IllegalArgumentException("ageInYears must not be negative.");
        }
        BigDecimal retained = BigDecimal.ONE.subtract(
                ANNUAL_DEPRECIATION.multiply(BigDecimal.valueOf(ageInYears)));
        if (retained.signum() < 0) {
            retained = BigDecimal.ZERO;
        }
        BigDecimal value = basePrice.getAmount().multiply(retained);
        return new Money(value, basePrice.currency());
    }
}
