package salon.catalog.domain.service;

import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.specification.VehicleSpecification;

/**
 * Serwis dziedzinowy (UC-KAT-01): łączy specyfikację z cennikiem.
 * W realnym systemie to on pobiera ProductCatalog z repozytorium i podaje go agregatowi —
 * sam agregat nie zna bazy. Tutaj jedynie deleguje walidację do agregatu (Fail-fast w addOption).
 */
public class RuleValidationDomainService {

    public void validateAndAdd(VehicleSpecification specification,
                               OptionCode option,
                               ProductCatalog catalog) {
        if (specification == null) {
            throw new IllegalArgumentException("specification must not be null.");
        }
        specification.addOption(option, catalog);
    }
}
