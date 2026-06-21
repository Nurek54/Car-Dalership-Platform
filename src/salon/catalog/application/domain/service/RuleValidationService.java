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
 * DOMAIN SERVICE (stateless) – validation of exclusion/requirement rules.
 *
 * Validation is not the entity's responsibility (PDF, chapter 3) – it requires access to
 * the whole state (selected options + catalog rules), so it is performed by a separate domain
 * service based on the Specification pattern. It throws an exception only after checking
 * wszystkiego.
 *
 * The class has no state, a single responsibility, and uses no repositories.
 */
public class RuleValidationService {

    /**
     * Verification of simple exclusion rules "on the fly" (UC-KON-01, step 4).
     * Throws {@link CombinationNotAllowedException} when the combination is blocked (A1).
     */
    public void validateSelection(VehicleSpecification specification, ProductCatalog catalog) {
        OptionCombinationSpecification spec = new OptionCombinationSpecification(catalog.rules());
        List<String> violations = spec.violations(specification.pickedAsSet());
        if (!violations.isEmpty()) {
            throw new CombinationNotAllowedException(
                    "Disallowed option combination: " + String.join("; ", violations));
        }
    }

    /**
     * Verification of final completeness and consistency before finalization
     * (UC-KON-01, step 6). Checks all REQUIRES/EXCLUDES rules.
     */
    public void validateComplete(VehicleSpecification specification, ProductCatalog catalog) {
        if (specification.optionsPicked().isEmpty()) {
            throw new CombinationNotAllowedException("The specification does not contain any option");
        }
        validateSelection(specification, catalog);
    }

    /**
     * Logical validation of the catalog (UC-KON-02, step 3): no conflicting rules –
     * the same pair of options cannot be both REQUIRES and EXCLUDES.
     * Structural validation (uniqueness of codes, existence of options) is performed by the factory.
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
                            "Conflicting rules for the pair " + rule.sourceCode() + "/" + rule.targetCode()
                                    + ": " + RuleType.REQUIRES + " and " + RuleType.EXCLUDES);
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
