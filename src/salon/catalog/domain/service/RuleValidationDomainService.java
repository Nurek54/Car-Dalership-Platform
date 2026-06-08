package salon.catalog.domain.service;

import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.specification.VehicleSpecification;

import java.util.Optional;

/**
 * Serwis dziedzinowy (UC-KAT-01): łączy specyfikację z cennikiem.
 * Zgodnie z diagramem architektury to ON czyta reguły z CatalogRepository — sam pobiera
 * ProductCatalog (po identyfikatorze ze specyfikacji) i podaje go agregatowi, dzięki czemu
 * agregat nie zna bazy, a walidacja reguł (Fail-fast) dzieje się w domenie (addOption).
 */
public class RuleValidationDomainService {

    private final CatalogRepository catalogRepository;

    public RuleValidationDomainService(CatalogRepository catalogRepository) {
        if (catalogRepository == null) {
            throw new IllegalArgumentException("catalogRepository must not be null.");
        }
        this.catalogRepository = catalogRepository;
    }

    /**
     * UC-KAT-01: koordynacja dodania opcji bez podawania cennika z zewnątrz.
     * Serwis sam pobiera właściwy cennik z repozytorium (po identyfikatorze ze specyfikacji),
     * a następnie zleca agregatowi dodanie opcji (pełna walidacja: obecność + wykluczenia + cena).
     */
    public void validateAndAddOption(VehicleSpecification specification, OptionCode option) {
        if (specification == null) {
            throw new IllegalArgumentException("specification must not be null.");
        }
        if (option == null) {
            throw new IllegalArgumentException("option must not be null.");
        }
        CatalogId catalogId = specification.getCatalogId();
        Optional<ProductCatalog> found = catalogRepository.findById(catalogId);
        if (found.isEmpty()) {
            throw new IllegalStateException("Catalog not found: " + catalogId.value());
        }
        specification.addOption(option, found.get());
    }
}
