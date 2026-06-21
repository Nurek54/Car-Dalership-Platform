package salon.catalog.application.dto;

import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.ModelYear;

import java.util.List;

/**
 * Input DTO of the {@link salon.catalog.application.port.out.CatalogImporterPort} port:
 * catalog data already TRANSLATED into the local model (domain value objects).
 *
 * Data from the "upstream" context is modeled as value objects (PDF, chapter 3).
 * We keep this record in the application.dto package, not in application.port — the port
 * remains solely an interface (rule: ports are interfaces). Building the aggregate
 * from this data is the task of the factory in the application service.
 */
public record ImportedCatalogData(ModelYear modelYear,
                                  List<CatalogOption> options,
                                  List<CatalogRule> rules) {
    public ImportedCatalogData {
        options = List.copyOf(options);
        rules = List.copyOf(rules);
    }
}
