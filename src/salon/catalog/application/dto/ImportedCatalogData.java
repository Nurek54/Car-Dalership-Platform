package salon.catalog.application.dto;

import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.ModelYear;

import java.util.List;

public record ImportedCatalogData(ModelYear modelYear,
                                  List<CatalogOption> options,
                                  List<CatalogRule> rules) {
    public ImportedCatalogData {
        options = List.copyOf(options);
        rules = List.copyOf(rules);
    }
}
