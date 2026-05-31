package unit.catalog_and_configurator_context;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class VehicleSpecificationTest {

    @Test
    void shouldSuccessfullyAddOptionWhenNoRulesAreViolated() {
        // Arrange (Given)
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(2000, "PLN")));

        VehicleSpecification specification = new VehicleSpecification(
                new SpecificationId("SPEC-123"),
                catalog.getCatalogId()
        );

        // Act (When)
        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);

        // Assert (Then)
        assertThat(specification.getSelectedOptions()).contains(new OptionCode("LED_LIGHTS"));
    }

    @Test
    void shouldThrowExceptionWhenAddingMutuallyExclusiveOption() {
        // Arrange (Given)
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        OptionCode manualGearbox = new OptionCode("MANUAL_GEARBOX");
        OptionCode adaptiveCruiseControl = new OptionCode("ADAPTIVE_CRUISE");

        catalog.addOption(new CatalogOption(manualGearbox, Money.of(0, "PLN")));
        catalog.addOption(new CatalogOption(adaptiveCruiseControl, Money.of(3000, "PLN")));

        // Definiowanie reguły wykluczającej w cenniku
        catalog.addRule(new CatalogRule(adaptiveCruiseControl, manualGearbox, RuleType.EXCLUDES));

        VehicleSpecification specification = new VehicleSpecification(
                new SpecificationId("SPEC-124"),
                catalog.getCatalogId()
        );

        // Dodajemy pierwszą opcję
        specification.addOption(manualGearbox, catalog);

        // Act & Assert (When & Then) - oczekujemy natychmiastowej blokady operacji (Fail-fast)
        assertThatThrownBy(() -> specification.addOption(adaptiveCruiseControl, catalog))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("Option ADAPTIVE_CRUISE is mutually exclusive with MANUAL_GEARBOX");
    }

    @Test
    void shouldFinalizeSpecificationWhenAllCardinalOptionsAreSelected() {
        // Arrange
        VehicleSpecification specification = new VehicleSpecification(
                new SpecificationId("SPEC-125"),
                new CatalogId("CAT-1")
        );
        // Symulacja dodania wymaganych opcji (silnik, skrzynia itp.)
        specification.addOption(new OptionCode("ENGINE_2_0_TSI"), mockCatalog);
        specification.addOption(new OptionCode("AUTO_GEARBOX"), mockCatalog);

        // Act
        specification.finalizeSpecification();

        // Assert - Weryfikacja, czy przed zmianą statusu na READY_FOR_SALES, specyfikacja posiada wszystkie wymagane opcje
        assertThat(specification.getState()).isEqualTo(SpecificationState.READY_FOR_SALES);
    }
}