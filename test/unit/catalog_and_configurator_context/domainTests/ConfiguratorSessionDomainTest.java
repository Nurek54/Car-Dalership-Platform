package unit.catalog_and_configurator_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.catalog.application.domain.exception.CombinationNotAllowedException;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.SpecificationState;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;
import salon.catalog.application.domain.service.RuleValidationService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** UC-KON-01: Opracowanie i zatwierdzenie specyfikacji pojazdu — przepływ na agregatach + walidacja reguł. */
class ConfiguratorSessionDomainTest {

    private final ProductCatalogFactory catalogFactory = new ProductCatalogFactory();
    private final VehicleSpecificationFactory specFactory = new VehicleSpecificationFactory();
    private final RuleValidationService ruleValidation = new RuleValidationService();

    private CatalogOption option(String code, String price) {
        return new CatalogOption(OptionCode.of(code), Money.of(new BigDecimal(price), "PLN"));
    }

    @Test
    void shouldBuildAndFinalizeValidSpecification() { // SCENARIUSZ GŁÓWNY
        // Cennik z regułą wykluczenia B2 EXCLUDES C1
        ProductCatalog catalog = catalogFactory.createNew(
                ModelYear.of(2025),
                List.of(option("B2", "10000"), option("C1", "5000"), option("A1", "2000")),
                List.of(new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.EXCLUDES)));

        // Klient dobiera niekolidujące opcje
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");
        spec.addOption(OptionCode.of("B2"), catalog);
        spec.addOption(OptionCode.of("A1"), catalog);

        // Walidacja całego stanu nie zgłasza naruszeń i specyfikacja może być sfinalizowana
        ruleValidation.validateComplete(spec, catalog);
        spec.finalizeSpecification();

        assertThat(spec.state()).isEqualTo(SpecificationState.FINAL);
        assertThat(spec.totalPrice()).isEqualTo(Money.of(new BigDecimal("12000"), "PLN"));
    }

    @Test
    void shouldBlockExcludedCombination() {       // Scenariusz alternatywny A1
        ProductCatalog catalog = catalogFactory.createNew(
                ModelYear.of(2025),
                List.of(option("B2", "10000"), option("C1", "5000")),
                List.of(new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.EXCLUDES)));

        // Klient próbuje połączyć silnik B2 z niedozwoloną skrzynią C1
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");
        spec.addOption(OptionCode.of("B2"), catalog);
        spec.addOption(OptionCode.of("C1"), catalog);

        // Reguła wykluczenia blokuje kombinację (wzorzec Fail-fast)
        assertThatThrownBy(() -> ruleValidation.validateSelection(spec, catalog))
                .isInstanceOf(CombinationNotAllowedException.class);
    }

    @Test
    void shouldRequireDependentOption() {
        // Reguła wymagania: A1 REQUIRES B2
        ProductCatalog catalog = catalogFactory.createNew(
                ModelYear.of(2025),
                List.of(option("A1", "2000"), option("B2", "10000")),
                List.of(new CatalogRule(OptionCode.of("A1"), OptionCode.of("B2"), RuleType.REQUIRES)));

        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");
        spec.addOption(OptionCode.of("A1"), catalog);

        // Brak wymaganej opcji B2 — kombinacja niedozwolona
        assertThatThrownBy(() -> ruleValidation.validateSelection(spec, catalog))
                .isInstanceOf(CombinationNotAllowedException.class);

        // Po dodaniu wymaganej opcji walidacja przechodzi
        spec.addOption(OptionCode.of("B2"), catalog);
        assertThatCode(() -> ruleValidation.validateSelection(spec, catalog)).doesNotThrowAnyException();
    }
}
