package salon.catalog.application.domain.model.catalog;

import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.Objects;

/**
 * Value object – a dependency rule between two equipment options.
 *
 * EXCLUDES example: source=B2 (engine), target=C1 (gearbox) – this combination
 * must not be finalized (UC-KON-01, alternative scenario A1).
 */
public final class CatalogRule {

    private final OptionCode sourceCode;
    private final OptionCode targetCode;
    private final RuleType type;

    public CatalogRule(OptionCode sourceCode, OptionCode targetCode, RuleType type) {
        this.sourceCode = Objects.requireNonNull(sourceCode, "sourceCode");
        this.targetCode = Objects.requireNonNull(targetCode, "targetCode");
        this.type = Objects.requireNonNull(type, "type");
        if (sourceCode.equals(targetCode)) {
            throw new IllegalArgumentException("A rule cannot bind an option to itself: " + sourceCode);
        }
    }

    public OptionCode sourceCode() {
        return sourceCode;
    }

    public OptionCode targetCode() {
        return targetCode;
    }

    public RuleType type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CatalogRule that)) return false;
        return sourceCode.equals(that.sourceCode)
                && targetCode.equals(that.targetCode)
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceCode, targetCode, type);
    }

    @Override
    public String toString() {
        return sourceCode + " " + type + " " + targetCode;
    }
}
