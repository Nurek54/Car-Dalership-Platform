package salon.catalog.application.domain.policy;

import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Specyfikacja sprawdzająca poprawność kombinacji wybranych opcji względem reguł
 * wykluczających (EXCLUDES) i wymagających (REQUIRES) z katalogu.
 *
 * Obiekt wartości – niezmienny, bez skutków ubocznych. Zastosowanie: WALIDACJA
 * (czy zbiór wybranych opcji spełnia reguły). Polityka państwa (PDF / Kanwa):
 * system NIE weryfikuje opcji wzajemnie, tylko na poziomie głównych kombinacji.
 */
public final class OptionCombinationSpecification implements Specification<Set<OptionCode>> {

    private final List<CatalogRule> rules;

    public OptionCombinationSpecification(List<CatalogRule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public boolean isSatisfiedBy(Set<OptionCode> pickedOptions) {
        return violations(pickedOptions).isEmpty();
    }

    /**
     * Zwraca opis naruszonych reguł (pusta lista = kombinacja dozwolona).
     * Operacja-zapytanie, bez modyfikacji stanu.
     */
    public List<String> violations(Set<OptionCode> pickedOptions) {
        List<String> violations = new ArrayList<>();
        for (CatalogRule rule : rules) {
            boolean sourcePicked = pickedOptions.contains(rule.sourceCode());
            if (!sourcePicked) {
                continue;
            }
            boolean targetPicked = pickedOptions.contains(rule.targetCode());

            if (rule.type() == RuleType.EXCLUDES && targetPicked) {
                violations.add("Opcja " + rule.sourceCode() + " wyklucza opcję " + rule.targetCode());
            }
            if (rule.type() == RuleType.REQUIRES && !targetPicked) {
                violations.add("Opcja " + rule.sourceCode() + " wymaga opcji " + rule.targetCode());
            }
        }
        return violations;
    }
}
