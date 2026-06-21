package salon.catalog.application.domain.model.catalog;

import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * AGGREGATE ROOT – Production Matrix / Price List.
 *
 * A complete set of options (CatalogOption) and dependency rules (CatalogRule) assigned
 * to a given model year. The root is the only access point to the interior of
 * the aggregate (Law of Demeter) and is responsible for checking the invariants.
 *
 * Invariants enforced by the aggregate:
 *  - the catalog has at least one option,
 *  - option codes are unique,
 *  - rules reference only options that exist in the catalog.
 *
 * Note (Rule 1): the strict separation between the rule dictionary (this aggregate) and
 * the user's configuration basket (VehicleSpecification) protects the system from
 * price changes during long-running offering.
 */
public class ProductCatalog {

    private final CatalogId id;
    private final ModelYear modelYear;
    private int version;
    private CatalogState state;
    private final List<CatalogOption> options;
    private final List<CatalogRule> rules;

    /**
     * Package-private constructor – invoked only by {@code ProductCatalogFactory}
     * (creation is not the responsibility of the aggregate or its client).
     */
    ProductCatalog(CatalogId id,
                   ModelYear modelYear,
                   int version,
                   CatalogState state,
                   List<CatalogOption> options,
                   List<CatalogRule> rules) {
        this.id = Objects.requireNonNull(id, "id");
        this.modelYear = Objects.requireNonNull(modelYear, "modelYear");
        this.version = version;
        this.state = Objects.requireNonNull(state, "state");
        this.options = new ArrayList<>(Objects.requireNonNull(options, "options"));
        this.rules = new ArrayList<>(Objects.requireNonNull(rules, "rules"));
    }

    /**
     * Stateful command: archives the catalog (UC-KON-02, step 4 – the previous data
     * is marked as archived after the new version is saved).
     */
    public void archive() {
        if (state == CatalogState.ARCHIVED) {
            return;
        }
        this.state = CatalogState.ARCHIVED;
    }

    /** Query: whether the given option code exists in this catalog. */
    public boolean containsOption(OptionCode code) {
        return findOption(code).isPresent();
    }

    /** Query: the base price of an option (throws an exception if the option is not in the catalog). */
    public Money priceOf(OptionCode code) {
        return findOption(code)
                .map(CatalogOption::basePrice)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Option " + code + " does not exist in catalog " + id));
    }

    private Optional<CatalogOption> findOption(OptionCode code) {
        return options.stream().filter(o -> o.code().equals(code)).findFirst();
    }

    /** Price list currency code (the catalog has at least one option – factory invariant). */
    public String currencyCode() {
        return options.get(0).basePrice().currency().getCurrencyCode();
    }

    public CatalogId id() {
        return id;
    }

    public ModelYear modelYear() {
        return modelYear;
    }

    public int version() {
        return version;
    }

    public CatalogState state() {
        return state;
    }

    /** Read-only (Law of Demeter – the client does not modify the aggregate's interior). */
    public List<CatalogOption> options() {
        return Collections.unmodifiableList(options);
    }

    public List<CatalogRule> rules() {
        return Collections.unmodifiableList(rules);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCatalog that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
