package salon.sales.application.domain.model.offer;

import java.math.BigDecimal;

// Value Object: żądany rabat wyrażony procentowo (np. 4.00 = 4%).
public record Discount(BigDecimal percentage) {

    public Discount {
        if (percentage == null) {
            throw new IllegalArgumentException("Discount percentage must not be null.");
        }
        if (percentage.signum() < 0) {
            throw new IllegalArgumentException("Discount percentage must not be negative.");
        }
    }
}
