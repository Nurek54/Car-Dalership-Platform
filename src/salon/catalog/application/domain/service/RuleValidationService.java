package salon.catalog.application.domain.service;

import salon.catalog.application.domain.exception.CatalogValidationException;
import salon.catalog.application.domain.exception.CombinationNotAllowedException;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.domain.policy.OptionCombinationSpecification;

import java.util.List;

/**
 * USŁUGA DZIEDZINY (bezstanowa) – walidacja reguł wykluczających/wymagających.
 *
 * Walidacja nie jest odpowiedzialnością encji (PDF, rozdz. 3) – wymaga dostępu do
 * całego stanu (wybrane opcje + reguły katalogu), więc realizuje ją osobna usługa
 * dziedziny w oparciu o wzorzec Specyfikacja. Zgłasza wyjątek dopiero po sprawdzeniu
 * wszystkiego.
 *
 * Klasa nie ma stanu, jedyna odpowiedzialność, nie używa repozytoriów.
 */
public class RuleValidationService {

    /**
     * Weryfikacja prostych reguł wykluczających „na bieżąco” (UC-KON-01, krok 4).
     * Rzuca {@link CombinationNotAllowedException}, gdy kombinacja jest zablokowana (A1).
     */
    public void validateSelection(VehicleSpecification specification, ProductCatalog catalog) {
        OptionCombinationSpecification spec = new OptionCombinationSpecification(catalog.rules());
        List<String> violations = spec.violations(specification.pickedAsSet());
        if (!violations.isEmpty()) {
            throw new CombinationNotAllowedException(
                    "Niedozwolona kombinacja opcji: " + String.join("; ", violations));
        }
    }

    /**
     * Weryfikacja ostatecznej kompletności i spójności przed zatwierdzeniem
     * (UC-KON-01, krok 6). Sprawdza wszystkie reguły REQUIRES/EXCLUDES.
     */
    public void validateComplete(VehicleSpecification specification, ProductCatalog catalog) {
        if (specification.optionsPicked().isEmpty()) {
            throw new CombinationNotAllowedException("Specyfikacja nie zawiera żadnej opcji");
        }
        validateSelection(specification, catalog);
    }

    /**
     * Walidacja logiczna katalogu (UC-KON-02, krok 3): brak reguł sprzecznych –
     * ta sama para opcji nie może być jednocześnie REQUIRES i EXCLUDES.
     * Walidację strukturalną (unikalność kodów, istnienie opcji) wykonuje fabryka.
     */
    public void validateCatalogConsistency(ProductCatalog catalog) {
        List<CatalogRule> rules = catalog.rules();
        for (CatalogRule rule : rules) {
            for (CatalogRule other : rules) {
                if (rule == other) {
                    continue;
                }
                boolean samePair = pairMatches(rule, other);
                if (samePair && rule.type() != other.type()) {
                    throw new CatalogValidationException(
                            "Sprzeczne reguły dla pary " + rule.sourceCode() + "/" + rule.targetCode()
                                    + ": " + RuleType.REQUIRES + " oraz " + RuleType.EXCLUDES);
                }
            }
        }
    }

    private boolean pairMatches(CatalogRule a, CatalogRule b) {
        OptionCode as = a.sourceCode();
        OptionCode at = a.targetCode();
        OptionCode bs = b.sourceCode();
        OptionCode bt = b.targetCode();
        return (as.equals(bs) && at.equals(bt)) || (as.equals(bt) && at.equals(bs));
    }
}
