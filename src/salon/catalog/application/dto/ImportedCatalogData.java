package salon.catalog.application.dto;

import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.ModelYear;

import java.util.List;

/**
 * DTO wejściowe portu {@link salon.catalog.application.port.out.CatalogImporterPort}:
 * dane katalogu już PRZETŁUMACZONE do modelu lokalnego (obiekty wartości dziedziny).
 *
 * Dane z kontekstu „na górze” modelujemy jako obiekty wartości (PDF, rozdz. 3).
 * Trzymamy ten rekord w pakiecie application.dto, a nie w application.port — port
 * pozostaje wyłącznie interfejsem (reguła: porty to interfejsy). Zbudowanie agregatu
 * z tych danych jest zadaniem fabryki w usłudze aplikacji.
 */
public record ImportedCatalogData(ModelYear modelYear,
                                  List<CatalogOption> options,
                                  List<CatalogRule> rules) {
    public ImportedCatalogData {
        options = List.copyOf(options);
        rules = List.copyOf(rules);
    }
}
