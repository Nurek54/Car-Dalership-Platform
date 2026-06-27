package salon.sales.application.domain.model.offer;

import java.math.BigDecimal;

public record Discount(BigDecimal percentage) {

    public Discount {
        if (percentage == null) {
            throw new IllegalArgumentException("percentage must not be null.");
        }
        if (percentage.signum() < 0 || percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("percentage must be within [0, 100].");
        }
    }
}
