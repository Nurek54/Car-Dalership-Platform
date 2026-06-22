package salon.catalog.application.domain.policy;

import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class OptionCombinationSpecification implements Specification<Set<OptionCode>> {

    private final List<CatalogRule> rules;

    public OptionCombinationSpecification(List<CatalogRule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public boolean isSatisfiedBy(Set<OptionCode> pickedOptions) {
        return violations(pickedOptions).isEmpty();
    }

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
