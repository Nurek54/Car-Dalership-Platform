package salon.catalog.application.domain.model.catalog;

import salon.catalog.application.domain.exception.CatalogValidationException;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Factory of the ProductCatalog aggregate.
 *
 * Creating a valid instance is not the responsibility of the aggregate or the client.
 * The factory encapsulates construction, guarantees the invariants and is atomic: it either returns
 * a valid catalog or throws an exception – it never returns an invalid object.
 *
 * Location: the domain layer (the factory expresses a domain concept – building the price list).
 */
public class ProductCatalogFactory {

    /**
     * Creates a new, ACTIVE catalog at version 1 with a new, global identifier.
     * Checks the invariants: a non-empty set of options, unique codes, rules pointing
     * only to options that exist in the catalog.
     */
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

    /**
     * Creates an ACTIVE catalog as the next version (UC-KON-02 – the new price list replaces the current one).
     */
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

    /**
     * Reconstitutes the aggregate from persistent storage (used by the repository adapter).
     * Does not validate business rules – the data comes from a trusted source.
     */
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
