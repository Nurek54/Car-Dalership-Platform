package salon.sales.domain.model.offer;

import java.math.BigDecimal;

// Value Object: maksymalny rabat, który Handlowiec może przyznać bez zgody Dyrektora.
public record DiscountLimit(BigDecimal maxAllowed) {

    public DiscountLimit {
        if (maxAllowed == null) {
            throw new IllegalArgumentException("DiscountLimit maxAllowed must not be null.");
        }
        if (maxAllowed.signum() < 0) {
            throw new IllegalArgumentException("DiscountLimit maxAllowed must not be negative.");
        }
    }
}
