package salon.catalog.application.domain.model.specification;

import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * AGGREGATE ROOT – the user's configuration basket (vehicle specification).
 *
 * References the ProductCatalog aggregate ONLY through {@link CatalogId}
 * (Rule 3 – references by identifier, not by reference). ProductCatalog
 * is passed as a command argument only to read prices and check
 * the invariants in a single transaction – it is not held as a field.
 *
 * Invariants:
 *  - only options that exist in the catalog can be selected,
 *  - the total price = the sum of the base prices of the selected options,
 *  - after finalization (FINAL) the specification is immutable.
 */
public class VehicleSpecification {

    private final SpecificationId id;
    private final CatalogId catalogId;
    private Money totalPrice;
    private SpecificationState state;
    private final Set<OptionCode> optionsPicked;

    /** Package-private constructor – invoked by {@code VehicleSpecificationFactory}. */
    VehicleSpecification(SpecificationId id,
                         CatalogId catalogId,
                         Money totalPrice,
                         SpecificationState state,
                         Set<OptionCode> optionsPicked) {
        this.id = Objects.requireNonNull(id, "id");
        this.catalogId = Objects.requireNonNull(catalogId, "catalogId");
        this.totalPrice = Objects.requireNonNull(totalPrice, "totalPrice");
        this.state = Objects.requireNonNull(state, "state");
        this.optionsPicked = new LinkedHashSet<>(Objects.requireNonNull(optionsPicked, "optionsPicked"));
    }

    /**
     * Stateful command: adds an option to the specification (UC-KON-01, steps 2–3).
     * Checks the invariant "the option must come from the catalog" and recomputes the total price.
     * Exclusion/requirement rules (EXCLUDES/REQUIRES) are validated by RuleValidationService
     * before this command is invoked – validating the whole state is not the entity's responsibility.
     */
    public void addOption(OptionCode code, ProductCatalog catalog) {
        ensureNotFinal();
        requireSameCatalog(catalog);
        if (!catalog.containsOption(code)) {
            throw new IllegalArgumentException(
                    "Option " + code + " does not belong to catalog " + catalogId);
        }
        optionsPicked.add(code);
        recalculateTotalPrice(catalog);
        markInProgress();
    }

    /** Stateful command: removes a previously added option and recomputes the price. */
    public void removeOption(OptionCode code, ProductCatalog catalog) {
        ensureNotFinal();
        requireSameCatalog(catalog);
        optionsPicked.remove(code);
        recalculateTotalPrice(catalog);
        markInProgress();
    }

    /**
     * Stateful command: finalizes the specification (UC-KON-01, steps 6–7).
     * Verifies final completeness (at least one option) and switches the state to FINAL.
     * Rule consistency should be confirmed by RuleValidationService before finalization.
     */
    public void finalizeSpecification() {
        ensureNotFinal();
        if (optionsPicked.isEmpty()) {
            throw new IllegalStateException("Cannot finalize an empty specification " + id);
        }
        this.state = SpecificationState.FINAL;
    }

    private void recalculateTotalPrice(ProductCatalog catalog) {
        Money sum = Money.zero(totalPrice.currency().getCurrencyCode());
        for (OptionCode code : optionsPicked) {
            sum = sum.add(catalog.priceOf(code));
        }
        this.totalPrice = sum;
    }

    private void ensureNotFinal() {
        if (state == SpecificationState.FINAL) {
            throw new IllegalStateException("Specification " + id + " is already finalized (FINAL)");
        }
    }

    private void markInProgress() {
        if (state == SpecificationState.DRAFT) {
            state = SpecificationState.IN_PROGRESS;
        }
    }

    private void requireSameCatalog(ProductCatalog catalog) {
        if (!catalog.id().equals(catalogId)) {
            throw new IllegalArgumentException(
                    "Catalog " + catalog.id() + " does not match the specification based on " + catalogId);
        }
    }

    public SpecificationId id() {
        return id;
    }

    public CatalogId catalogId() {
        return catalogId;
    }

    public Money totalPrice() {
        return totalPrice;
    }

    public SpecificationState state() {
        return state;
    }

    public List<OptionCode> optionsPicked() {
        return Collections.unmodifiableList(new ArrayList<>(optionsPicked));
    }

    /** Returns a copy of the set of selected options (for RuleValidationService / specification). */
    public Set<OptionCode> pickedAsSet() {
        return new LinkedHashSet<>(optionsPicked);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleSpecification that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
