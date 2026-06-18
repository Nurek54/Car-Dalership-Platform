package salon.catalog.infrastructure.out.persistence;

import salon.common.infrastructure.persistence.DomainReflection;
import org.springframework.stereotype.Component;
import salon.catalog.application.port.out.CatalogDatabaseRepository;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.CatalogState;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.OptionCode;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.common.model.Money;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter sterowany (driven) — persystencja cennika (port {@link CatalogDatabaseRepository}).
 *
 * Odtworzenie buduje aktywny cennik, dokłada opcje/reguły przez metody domeny, a końcowy stan
 * (np. ARCHIVED) ustawia refleksją w infrastrukturze — bez modyfikacji kodu Katalogu.
 */
@Component
public class ProductCatalogDatabaseAdapter implements CatalogDatabaseRepository {

    private final ProductCatalogJpaRepository repository;

    public ProductCatalogDatabaseAdapter(ProductCatalogJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(ProductCatalog catalog) {
        repository.save(toEntity(catalog));
    }

    @Override
    public Optional<ProductCatalog> findById(CatalogId id) {
        return repository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<ProductCatalog> findAll() {
        List<ProductCatalogJpaEntity> entities = repository.findAll();
        List<ProductCatalog> result = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            result.add(toDomain(entities.get(i)));
        }
        return result;
    }

    private ProductCatalogJpaEntity toEntity(ProductCatalog catalog) {
        ProductCatalogJpaEntity entity = new ProductCatalogJpaEntity();
        entity.id = catalog.getCatalogId().value();
        entity.modelYear = catalog.getModelYear().value();
        entity.version = catalog.getVersion();
        entity.state = catalog.getState().name();
        entity.options = new ArrayList<>();
        List<CatalogOption> options = catalog.getOptions();
        for (int i = 0; i < options.size(); i++) {
            CatalogOption option = options.get(i);
            CatalogOptionEmbeddable embeddable = new CatalogOptionEmbeddable();
            embeddable.code = option.code().value();
            embeddable.price = option.basePrice().amount();
            embeddable.currency = option.basePrice().currency();
            entity.options.add(embeddable);
        }
        entity.rules = new ArrayList<>();
        List<CatalogRule> rules = catalog.getRules();
        for (int i = 0; i < rules.size(); i++) {
            CatalogRule rule = rules.get(i);
            CatalogRuleEmbeddable embeddable = new CatalogRuleEmbeddable();
            embeddable.sourceCode = rule.sourceCode().value();
            embeddable.targetCode = rule.targetCode().value();
            embeddable.type = rule.type().name();
            entity.rules.add(embeddable);
        }
        return entity;
    }

    private ProductCatalog toDomain(ProductCatalogJpaEntity entity) {
        // Budujemy jako ACTIVE, aby móc dołożyć opcje/reguły, a docelowy stan ustawiamy na końcu.
        ProductCatalog catalog = new ProductCatalog(
                new CatalogId(entity.id), new ModelYear(entity.modelYear), entity.version, CatalogState.ACTIVE);
        for (int i = 0; i < entity.options.size(); i++) {
            CatalogOptionEmbeddable option = entity.options.get(i);
            catalog.addOption(new CatalogOption(new OptionCode(option.code), Money.of(option.price, option.currency)));
        }
        for (int i = 0; i < entity.rules.size(); i++) {
            CatalogRuleEmbeddable rule = entity.rules.get(i);
            catalog.addRule(new CatalogRule(
                    new OptionCode(rule.sourceCode), new OptionCode(rule.targetCode), RuleType.valueOf(rule.type)));
        }
        DomainReflection.set(catalog, "state", CatalogState.valueOf(entity.state));
        return catalog;
    }
}
