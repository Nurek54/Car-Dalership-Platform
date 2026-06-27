package salon.catalog.application.domain.model.catalog;

import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.Objects;

public final class CatalogOption {

    private final OptionCode code;
    private final Money basePrice;

    public CatalogOption(OptionCode code, Money basePrice) {
        this.code = Objects.requireNonNull(code, "code");
        this.basePrice = Objects.requireNonNull(basePrice, "basePrice");
    }

    public OptionCode code() {
        return code;
    }

    public Money basePrice() {
        return basePrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CatalogOption that)) return false;
        return code.equals(that.code) && basePrice.equals(that.basePrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, basePrice);
    }

    @Override
    public String toString() {
        return "CatalogOption{" + code + " = " + basePrice + '}';
    }
}
