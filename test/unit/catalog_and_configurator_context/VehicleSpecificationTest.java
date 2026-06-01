package unit.catalog_and_configurator_context;

import org.junit.jupiter.api.Test;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.CatalogRule;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.catalog.RuleType;
import salon.catalog.domain.model.specification.RuleViolationException;
import salon.catalog.domain.model.specification.SpecificationState;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import static org.assertj.core.api.Assertions.*;

class VehicleSpecificationTest {

    @Test
    void shouldSuccessfullyAddOptionWhenNoRulesAreViolated() {
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(2000, "PLN")));

        VehicleSpecification specification = new VehicleSpecification(
                new SpecificationId("SPEC-123"), catalog.getCatalogId());

        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);

        assertThat(specification.getSelectedOptions()).contains(new OptionCode("LED_LIGHTS"));
    }

    @Test
    void shouldThrowExceptionWhenAddingMutuallyExclusiveOption() {
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        OptionCode manualGearbox = new OptionCode("MANUAL_GEARBOX");
        OptionCode adaptiveCruiseControl = new OptionCode("ADAPTIVE_CRUISE");

        catalog.addOption(new CatalogOption(manualGearbox, Money.of(0, "PLN")));
        catalog.addOption(new CatalogOption(adaptiveCruiseControl, Money.of(3000, "PLN")));
        catalog.addRule(new CatalogRule(adaptiveCruiseControl, manualGearbox, RuleType.EXCLUDES));

        VehicleSpecification specification = new VehicleSpecification(
                new SpecificationId("SPEC-124"), catalog.getCatalogId());

        specification.addOption(manualGearbox, catalog);

        assertThatThrownBy(() -> specification.addOption(adaptiveCruiseControl, catalog))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("Option ADAPTIVE_CRUISE is mutually exclusive with MANUAL_GEARBOX");
    }

    @Test
    void shouldFinalizeSpecificationWhenAllCardinalOptionsAreSelected() {
        // Poprawione: oryginalny test używał niezadeklarowanego 'mockCatalog' (nie kompilował się).
        // Tworzymy realny cennik z wymaganymi opcjami (silnik + skrzynia).
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(new CatalogOption(new OptionCode("ENGINE_2_0_TSI"), Money.of(15000, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("AUTO_GEARBOX"), Money.of(8000, "PLN")));

        VehicleSpecification specification = new VehicleSpecification(
                new SpecificationId("SPEC-125"), catalog.getCatalogId());

        specification.addOption(new OptionCode("ENGINE_2_0_TSI"), catalog);
        specification.addOption(new OptionCode("AUTO_GEARBOX"), catalog);

        specification.finalizeSpecification();

        assertThat(specification.getState()).isEqualTo(SpecificationState.READY_FOR_SALES);
    }
}
