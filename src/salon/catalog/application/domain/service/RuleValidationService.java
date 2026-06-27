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

public class RuleValidationService {

    
    public void validateSelection(VehicleSpecification specification, ProductCatalog catalog) {
        OptionCombinationSpecification spec = new OptionCombinationSpecification(catalog.rules());
        List<String> violations = spec.violations(specification.pickedAsSet());
        if (!violations.isEmpty()) {
            throw new CombinationNotAllowedException(
                    "Disallowed option combination: " + String.join("; ", violations));
        }
    }

    
    public void validateComplete(VehicleSpecification specification, ProductCatalog catalog) {
        if (specification.optionsPicked().isEmpty()) {
            throw new CombinationNotAllowedException("The specification does not contain any option");
        }
        validateSelection(specification, catalog);
    }

    
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
