package salon.catalog.domain.service;

import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.CatalogRule;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.catalog.RuleType;
import salon.catalog.domain.model.specification.RuleViolationException;
import salon.catalog.domain.model.specification.VehicleSpecification;

import java.util.List;
import java.util.Optional;

/**
 * Serwis dziedzinowy (UC-KON-01): łączy specyfikację z cennikiem.
 * Zgodnie z diagramem architektury to ON czyta reguły z CatalogRepository — sam pobiera
 * ProductCatalog (po identyfikatorze ze specyfikacji) i podaje go agregatowi, dzięki czemu
 * agregat nie zna bazy, a walidacja reguł (Fail-fast) dzieje się w domenie (addOption).
 *
 * Bezstanowy: koordynuje dodawanie opcji (EXCLUDES, Fail-fast) oraz ocenia kompletność
 * konfiguracji przed finalizacją (REQUIRES).
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
     * UC-KON-01: koordynacja dodania opcji bez podawania cennika z zewnątrz.
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
        specification.addOption(option, loadCatalog(specification));
    }

    /**
     * UC-KON-01 (reguła finalizacji): ocena kompletności konfiguracji względem reguł cennika.
     * Sprawdzamy reguły REQUIRES — jeśli wybrano opcję źródłową, musi też być obecna opcja wymagana.
     * Złamanie kompletności blokuje finalizację (RuleViolationException).
     *
     * Uwaga: kompletność grup kardynalnych (silnik/skrzynia/kolor) wymaga kategoryzacji opcji,
     * której bieżący model (CatalogOption = kod + cena) nie posiada — egzekwujemy tu zależności REQUIRES.
     */
    public void assertComplete(VehicleSpecification specification) {
        if (specification == null) {
            throw new IllegalArgumentException("specification must not be null.");
        }
        ProductCatalog catalog = loadCatalog(specification);
        List<OptionCode> picked = specification.getSelectedOptions();
        List<CatalogRule> rules = catalog.getRules();
        for (int i = 0; i < rules.size(); i++) {
            CatalogRule rule = rules.get(i);
            if (rule.type() != RuleType.REQUIRES) {
                continue;
            }
            if (picked.contains(rule.sourceCode()) && !picked.contains(rule.targetCode())) {
                throw new RuleViolationException("Option " + rule.sourceCode().value()
                        + " requires " + rule.targetCode().value() + " to be selected.");
            }
        }
    }

    private ProductCatalog loadCatalog(VehicleSpecification specification) {
        CatalogId catalogId = specification.getCatalogId();
        Optional<ProductCatalog> found = catalogRepository.findById(catalogId);
        if (found.isEmpty()) {
            throw new IllegalStateException("Catalog not found: " + catalogId.value());
        }
        return found.get();
    }
}
