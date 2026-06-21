package salon.catalog.application.domain.policy;

import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A specification that checks the validity of the selected option combination against the
 * exclusion (EXCLUDES) and requirement (REQUIRES) rules from the catalog.
 *
 * Value object – immutable, without side effects. Purpose: VALIDATION
 * (whether the set of selected options satisfies the rules). Policy (PDF / Canvas):
 * the system does NOT verify options against each other, only at the level of the main combinations.
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
     * Returns a description of the violated rules (an empty list = the combination is allowed).
     * A query operation, without modifying state.
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
                violations.add("Option " + rule.sourceCode() + " excludes option " + rule.targetCode());
            }
            if (rule.type() == RuleType.REQUIRES && !targetPicked) {
                violations.add("Option " + rule.sourceCode() + " requires option " + rule.targetCode());
            }
        }
        return violations;
    }
}
