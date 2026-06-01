package salon.catalog.application.service;

import salon.catalog.application.port.in.BuildSpecificationUseCase;
import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.application.port.out.SpecificationRepository;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.catalog.domain.service.RuleValidationDomainService;
import salon.shared.model.SpecificationId;

import java.util.Optional;

/**
 * Realizuje UC-KAT-01 (orkiestracja). Pobiera cennik z repozytorium i podaje go agregatowi —
 * dzięki temu walidacja reguł (Fail-fast) dzieje się w domenie, a aplikacja jest cienka.
 */
public class SpecificationAppService implements BuildSpecificationUseCase {

    private final SpecificationRepository specificationRepository;
    private final CatalogRepository catalogRepository;
    private final RuleValidationDomainService ruleValidation;

    public SpecificationAppService(SpecificationRepository specificationRepository,
                                   CatalogRepository catalogRepository,
                                   RuleValidationDomainService ruleValidation) {
        if (specificationRepository == null) {
            throw new IllegalArgumentException("specificationRepository must not be null.");
        }
        if (catalogRepository == null) {
            throw new IllegalArgumentException("catalogRepository must not be null.");
        }
        if (ruleValidation == null) {
            throw new IllegalArgumentException("ruleValidation must not be null.");
        }
        this.specificationRepository = specificationRepository;
        this.catalogRepository = catalogRepository;
        this.ruleValidation = ruleValidation;
    }

    @Override
    public SpecificationId startSpecification(String catalogId) {
        ProductCatalog catalog = loadCatalog(catalogId);
        VehicleSpecification specification =
                new VehicleSpecification(SpecificationId.generate(), catalog.getCatalogId());
        specificationRepository.save(specification);
        return specification.getId();
    }

    @Override
    public void addOption(String specificationId, String catalogId, String optionCode) {
        VehicleSpecification specification = loadSpecification(specificationId);
        ProductCatalog catalog = loadCatalog(catalogId);
        ruleValidation.validateAndAdd(specification, new OptionCode(optionCode), catalog);
        specificationRepository.save(specification);
    }

    @Override
    public void finalizeSpecification(String specificationId) {
        VehicleSpecification specification = loadSpecification(specificationId);
        specification.finalizeSpecification();
        specificationRepository.save(specification);
    }

    private ProductCatalog loadCatalog(String catalogId) {
        Optional<ProductCatalog> found = catalogRepository.findById(new CatalogId(catalogId));
        if (found.isEmpty()) {
            throw new IllegalStateException("Catalog not found: " + catalogId);
        }
        return found.get();
    }

    private VehicleSpecification loadSpecification(String specificationId) {
        Optional<VehicleSpecification> found =
                specificationRepository.findById(new SpecificationId(specificationId));
        if (found.isEmpty()) {
            throw new IllegalStateException("Specification not found: " + specificationId);
        }
        return found.get();
    }
}
