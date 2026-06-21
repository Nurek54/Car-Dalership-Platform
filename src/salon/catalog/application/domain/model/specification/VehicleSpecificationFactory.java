package salon.catalog.application.domain.model.specification;

import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.Set;

/**
 * Factory of the VehicleSpecification aggregate.
 *
 * Creates a specification in a consistent initial state: a new global identifier,
 * the DRAFT state, a zero total price and an empty set of options. Atomic – never returns
 * obiektu niepoprawnego.
 */
public class VehicleSpecificationFactory {

    /**
     * Opening a configurator session (UC-KON-01, step 1) – creates a working specification
     * linked to the active catalog through {@link CatalogId}.
     */
    public VehicleSpecification createDraft(CatalogId catalogId, String currencyCode) {
        return new VehicleSpecification(
                SpecificationId.generate(),
                catalogId,
                Money.zero(currencyCode),
                SpecificationState.DRAFT,
                Set.of());
    }

    /** Reconstitutes the aggregate from persistent storage (used by the repository adapter). */
    public VehicleSpecification reconstitute(SpecificationId id,
                                             CatalogId catalogId,
                                             Money totalPrice,
                                             SpecificationState state,
                                             Set<OptionCode> optionsPicked) {
        return new VehicleSpecification(id, catalogId, totalPrice, state, optionsPicked);
    }
}
