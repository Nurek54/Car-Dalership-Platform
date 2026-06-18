package salon.catalog.application.domain.model.catalog;

// Encja LOKALNA cennika: reguła zależności między opcjami (sourceCode --type--> targetCode).
public record CatalogRule(OptionCode sourceCode, OptionCode targetCode, RuleType type) {

    public CatalogRule {
        if (sourceCode == null) {
            throw new IllegalArgumentException("Rule sourceCode must not be null.");
        }
        if (targetCode == null) {
            throw new IllegalArgumentException("Rule targetCode must not be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Rule type must not be null.");
        }
    }
}
