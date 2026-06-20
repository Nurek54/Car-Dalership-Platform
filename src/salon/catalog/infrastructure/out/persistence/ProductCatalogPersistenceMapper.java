package salon.catalog.infrastructure.out.persistence;

import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.CatalogState;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapowanie dwukierunkowe między agregatem ProductCatalog a rekordem magazynu.
 * Odtworzenie agregatu realizuje fabryka ({@code reconstitute}) – budowa egzemplarza
 * nie jest odpowiedzialnością mappera.
 */
@Component
public class ProductCatalogPersistenceMapper {

    private final ProductCatalogFactory factory;

    public ProductCatalogPersistenceMapper(ProductCatalogFactory factory) {
        this.factory = factory;
    }

    public ProductCatalogRecord toRecord(ProductCatalog catalog) {
        List<ProductCatalogRecord.OptionRecord> options = catalog.options().stream()
                .map(o -> new ProductCatalogRecord.OptionRecord(
                        o.code().value(),
                        o.basePrice().amount(),
                        o.basePrice().currency().getCurrencyCode()))
                .toList();

        List<ProductCatalogRecord.RuleRecord> rules = catalog.rules().stream()
                .map(r -> new ProductCatalogRecord.RuleRecord(
                        r.sourceCode().value(),
                        r.targetCode().value(),
                        r.type().name()))
                .toList();

        return new ProductCatalogRecord(
                catalog.id().toString(),
                catalog.modelYear().year(),
                catalog.version(),
                catalog.state().name(),
                options,
                rules);
    }

    public ProductCatalog toDomain(ProductCatalogRecord record) {
        List<CatalogOption> options = record.options().stream()
                .map(o -> new CatalogOption(
                        OptionCode.of(o.code()),
                        Money.of(o.price(), o.currency())))
                .toList();

        List<CatalogRule> rules = record.rules().stream()
                .map(r -> new CatalogRule(
                        OptionCode.of(r.sourceCode()),
                        OptionCode.of(r.targetCode()),
                        RuleType.valueOf(r.type())))
                .toList();

        return factory.reconstitute(
                CatalogId.of(record.id()),
                ModelYear.of(record.modelYear()),
                record.version(),
                CatalogState.valueOf(record.state()),
                options,
                rules);
    }
}
