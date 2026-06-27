package salon.catalog.application.domain.model.catalog;

import salon.catalog.application.domain.exception.CatalogValidationException;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductCatalogFactory {

    
    public ProductCatalog createNew(ModelYear modelYear,
                                    List<CatalogOption> options,
                                    List<CatalogRule> rules) {
        validate(options, rules);
        return new ProductCatalog(
                CatalogId.generate(),
                modelYear,
                1,
                CatalogState.ACTIVE,
                options,
                rules);
    }

    
    public ProductCatalog createNextVersion(ModelYear modelYear,
                                            int newVersion,
                                            List<CatalogOption> options,
                                            List<CatalogRule> rules) {
        validate(options, rules);
        return new ProductCatalog(
                CatalogId.generate(),
                modelYear,
                newVersion,
                CatalogState.ACTIVE,
                options,
                rules);
    }

    
    public ProductCatalog reconstitute(CatalogId id,
                                       ModelYear modelYear,
                                       int version,
                                       CatalogState state,
                                       List<CatalogOption> options,
                                       List<CatalogRule> rules) {
        return new ProductCatalog(id, modelYear, version, state, options, rules);
    }

    private void validate(List<CatalogOption> options, List<CatalogRule> rules) {
        if (options == null || options.isEmpty()) {
            throw new CatalogValidationException("The catalog must contain at least one option");
        }
        Set<OptionCode> codes = new HashSet<>();
        for (CatalogOption option : options) {
            if (!codes.add(option.code())) {
                throw new CatalogValidationException("Duplicate option code: " + option.code());
            }
        }
        if (rules != null) {
            for (CatalogRule rule : rules) {
                if (!codes.contains(rule.sourceCode())) {
                    throw new CatalogValidationException(
                            "The rule references a non-existent option: " + rule.sourceCode());
                }
                if (!codes.contains(rule.targetCode())) {
                    throw new CatalogValidationException(
                            "The rule references a non-existent option: " + rule.targetCode());
                }
            }
        }
    }
}
