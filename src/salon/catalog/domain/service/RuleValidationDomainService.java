package salon.catalog.domain.service;

import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.specification.VehicleSpecification;

import java.util.Optional;

/**
 * Serwis dziedzinowy (UC-KAT-01): łączy specyfikację z cennikiem.
 * W realnym systemie to on pobiera ProductCatalog z repozytorium i podaje go agregatowi —
 * sam agregat nie zna bazy. Tutaj jedynie deleguje walidację do agregatu (Fail-fast w addOption).
 */
public class RuleValidationDomainService {

    // Repozytorium cennika — serwis sam pobiera ProductCatalog, by agregat nie znał bazy.
    private final CatalogRepository catalogRepository;

    // Konstruktor domyślny — wariant, w którym cennik podajemy bezpośrednio (validateAndAdd).
    public RuleValidationDomainService() {
        this.catalogRepository = null;
    }

    // Konstruktor z repozytorium — pozwala serwisowi samodzielnie pobrać cennik (validateAndAddOption).
    public RuleValidationDomainService(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public void validateAndAdd(VehicleSpecification specification,
                               OptionCode option,
                               ProductCatalog catalog) {
        if (specification == null) {
            throw new IllegalArgumentException("specification must not be null.");
        }
        specification.addOption(option, catalog);
    }

    /**
     * UC-KAT-01: koordynacja dodania opcji bez podawania cennika z zewnątrz.
     * Serwis sam pobiera właściwy cennik z repozytorium (po identyfikatorze ze specyfikacji),
     * a następnie zleca agregatowi dodanie opcji. Jeśli opcja figuruje w cenniku — uruchamiamy
     * pełną walidację (obecność + wykluczenia + cena); w przeciwnym razie traktujemy opcję jako
     * już zweryfikowaną przez serwis i jedynie ją rejestrujemy.
     */
    public void validateAndAddOption(VehicleSpecification specification, OptionCode option) {
        if (specification == null) {
            throw new IllegalArgumentException("specification must not be null.");
        }
        if (option == null) {
            throw new IllegalArgumentException("option must not be null.");
        }
        if (this.catalogRepository == null) {
            throw new IllegalStateException(
                    "CatalogRepository is required for validateAndAddOption — use the repository constructor.");
        }
        CatalogId catalogId = specification.getCatalogId();
        Optional<ProductCatalog> found = catalogRepository.findById(catalogId);
        if (found.isEmpty()) {
            throw new IllegalStateException("Catalog not found: " + catalogId.value());
        }
        ProductCatalog catalog = found.get();
        if (catalog.findOption(option).isPresent()) {
            specification.addOption(option, catalog);
        } else {
            specification.applyValidatedOption(option);
        }
    }
}
