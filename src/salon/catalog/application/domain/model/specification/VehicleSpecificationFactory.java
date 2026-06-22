package salon.catalog.application.domain.model.specification;

import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.Set;

public class VehicleSpecificationFactory {

    public VehicleSpecification createDraft(CatalogId catalogId, String currencyCode) {
        return new VehicleSpecification(
                SpecificationId.generate(),
                catalogId,
                Money.zero(currencyCode),
                SpecificationState.DRAFT,
                Set.of());
    }

    public VehicleSpecification reconstitute(SpecificationId id,
                                             CatalogId catalogId,
                                             Money totalPrice,
                                             SpecificationState state,
                                             Set<OptionCode> optionsPicked) {
        return new VehicleSpecification(id, catalogId, totalPrice, state, optionsPicked);
    }
}
