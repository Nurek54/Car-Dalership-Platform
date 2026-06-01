package salon.catalog.infrastructure.mock;

import salon.catalog.application.port.out.ImporterApiPort;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.shared.model.Money;

import java.util.ArrayList;
import java.util.List;

/**
 * Udawane API Importera (ACL) — zwraca przykładowy zestaw opcji dla nowego cennika.
 */
public class ImporterApiMockAdapter implements ImporterApiPort {

    @Override
    public List<CatalogOption> fetchCurrentOptions(String modelYear) {
        List<CatalogOption> options = new ArrayList<>();
        options.add(new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(2000, "PLN")));
        options.add(new CatalogOption(new OptionCode("ENGINE_2_0_TSI"), Money.of(15000, "PLN")));
        options.add(new CatalogOption(new OptionCode("AUTO_GEARBOX"), Money.of(8000, "PLN")));
        return options;
    }
}
