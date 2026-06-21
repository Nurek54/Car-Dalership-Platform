package unit.catalog_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.catalog.domain.event.SpecificationCompletedEvent;
import salon.catalog.domain.model.catalog.*;
import salon.catalog.domain.model.specification.RuleViolationException;
import salon.catalog.domain.model.specification.SpecificationState;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import static org.assertj.core.api.Assertions.*;

class VehicleSpecificationTest {

    // Test price list: two paid options + a mutual-exclusion rule (UC-KON-01)
    private ProductCatalog createCatalogWithExclusion() {
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(new CatalogOption(new OptionCode("PANORAMIC_ROOF"), Money.of(8000, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("ROOF_RAILS"), Money.of(1500, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(4500, "PLN")));
        catalog.addRule(new CatalogRule(
                new OptionCode("PANORAMIC_ROOF"), new OptionCode("ROOF_RAILS"), RuleType.EXCLUDES));
        return catalog;
    }

    private VehicleSpecification createSpecification(ProductCatalog catalog) {
        return new VehicleSpecification(new SpecificationId("SPEC-1"), catalog.getCatalogId());
    }

    @Test
    void shouldAddOptionsAndAccumulateTotalPrice() {
        ProductCatalog catalog = createCatalogWithExclusion();
        VehicleSpecification specification = createSpecification(catalog);

        // A new configuration starts as DRAFT without pricing
        assertThat(specification.state()).isEqualTo(SpecificationState.DRAFT);
        assertThat(specification.getTotalPrice()).isNull();

        // The customer selects two options from the price list
        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);
        specification.addOption(new OptionCode("PANORAMIC_ROOF"), catalog);

        // The configuration transitions to IN_PROGRESS, and the price sums up from the base prices
        assertThat(specification.state()).isEqualTo(SpecificationState.IN_PROGRESS);
        assertThat(specification.getTotalPrice()).isEqualTo(Money.of(12500, "PLN"));
        assertThat(specification.getSelectedOptions())
                .containsExactly(new OptionCode("LED_LIGHTS"), new OptionCode("PANORAMIC_ROOF"));
    }

    @Test
    void shouldRejectOptionAbsentFromCatalog() {
        ProductCatalog catalog = createCatalogWithExclusion();
        VehicleSpecification specification = createSpecification(catalog);

        // An option not in the price list -> immediate refusal
        assertThatThrownBy(() -> specification.addOption(new OptionCode("V12_ENGINE"), catalog))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available in the catalog");
    }

    @Test
    void shouldFailFastWhenNewOptionExcludesAlreadySelectedOne() {
        ProductCatalog catalog = createCatalogWithExclusion();
        VehicleSpecification specification = createSpecification(catalog);

        // Najpierw wybrano relingi dachowe
        specification.addOption(new OptionCode("ROOF_RAILS"), catalog);

        // A panoramic roof is mutually exclusive with the roof rails -> Technological Validation (Fail-fast)
        assertThatThrownBy(() -> specification.addOption(new OptionCode("PANORAMIC_ROOF"), catalog))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("mutually exclusive");

        // The configuration stays intact (the faulty option did not enter)
        assertThat(specification.getSelectedOptions()).containsExactly(new OptionCode("ROOF_RAILS"));
    }

    @Test
    void shouldFailFastInBothDirectionsOfExclusionRule() {
        ProductCatalog catalog = createCatalogWithExclusion();
        VehicleSpecification specification = createSpecification(catalog);

        // The rule works "from the other side" too: first the rule source, then the target
        specification.addOption(new OptionCode("PANORAMIC_ROOF"), catalog);

        assertThatThrownBy(() -> specification.addOption(new OptionCode("ROOF_RAILS"), catalog))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("mutually exclusive");
    }

    @Test
    void shouldReturnToDraftWhenLastOptionIsRemoved() {
        ProductCatalog catalog = createCatalogWithExclusion();
        VehicleSpecification specification = createSpecification(catalog);

        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);
        assertThat(specification.state()).isEqualTo(SpecificationState.IN_PROGRESS);

        // The customer removes the only selected option
        specification.removeOption(new OptionCode("LED_LIGHTS"));

        // An empty configuration returns to DRAFT
        assertThat(specification.state()).isEqualTo(SpecificationState.DRAFT);
        assertThat(specification.getSelectedOptions()).isEmpty();
    }

    @Test
    void shouldFinalizeSpecificationAndRegisterDomainEvent() {
        ProductCatalog catalog = createCatalogWithExclusion();
        VehicleSpecification specification = createSpecification(catalog);
        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);

        // Closing the configuration (UC-KON-01)
        specification.finalizeSpecification();

        // The FINAL state + the SpecificationCompleted event recorded in the aggregate
        assertThat(specification.state()).isEqualTo(SpecificationState.FINAL);
        assertThat(specification.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(SpecificationCompletedEvent.class)
                .satisfies(event -> {
                    SpecificationCompletedEvent e = (SpecificationCompletedEvent) event;
                    assertThat(e.specificationId()).isEqualTo("SPEC-1");
                    assertThat(e.catalogId()).isEqualTo(catalog.getCatalogId().value());
                });
    }

    @Test
    void shouldRejectFinalizationOfEmptySpecification() {
        ProductCatalog catalog = createCatalogWithExclusion();
        VehicleSpecification specification = createSpecification(catalog);

        // An empty configuration must not be finalized
        assertThatThrownBy(specification::finalizeSpecification)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least one option");
    }

    @Test
    void shouldBlockAnyModificationAfterFinalization() {
        ProductCatalog catalog = createCatalogWithExclusion();
        VehicleSpecification specification = createSpecification(catalog);
        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);
        specification.finalizeSpecification();

        // A finalized specification is immutable — neither adding nor removing an option
        assertThatThrownBy(() -> specification.addOption(new OptionCode("PANORAMIC_ROOF"), catalog))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("finalized");
        assertThatThrownBy(() -> specification.removeOption(new OptionCode("LED_LIGHTS")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("finalized");
    }
}
