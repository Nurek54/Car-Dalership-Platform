package salon.catalog.application.domain.model.catalog;

import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.Objects;

/**
 * Obiekt wartości – reguła zależności między dwiema opcjami wyposażenia.
 *
 * Przykład EXCLUDES: source=B2 (silnik), target=C1 (skrzynia) – tej kombinacji
 * nie wolno zatwierdzić (UC-KON-01, scenariusz alternatywny A1).
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
            throw new IllegalArgumentException("Reguła nie może wiązać opcji z samą sobą: " + sourceCode);
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
