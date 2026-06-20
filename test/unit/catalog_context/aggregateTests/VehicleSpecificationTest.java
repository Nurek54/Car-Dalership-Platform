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

    // Cennik testowy: dwie opcje płatne + reguła wzajemnego wykluczenia (UC-KON-01)
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

        // Nowa konfiguracja startuje jako DRAFT bez wyceny
        assertThat(specification.state()).isEqualTo(SpecificationState.DRAFT);
        assertThat(specification.getTotalPrice()).isNull();

        // Klient dobiera dwie opcje z cennika
        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);
        specification.addOption(new OptionCode("PANORAMIC_ROOF"), catalog);

        // Konfiguracja przechodzi w IN_PROGRESS, a cena sumuje się z cen bazowych
        assertThat(specification.state()).isEqualTo(SpecificationState.IN_PROGRESS);
        assertThat(specification.getTotalPrice()).isEqualTo(Money.of(12500, "PLN"));
        assertThat(specification.getSelectedOptions())
                .containsExactly(new OptionCode("LED_LIGHTS"), new OptionCode("PANORAMIC_ROOF"));
    }

    @Test
    void shouldRejectOptionAbsentFromCatalog() {
        ProductCatalog catalog = createCatalogWithExclusion();
        VehicleSpecification specification = createSpecification(catalog);

        // Opcja spoza cennika -> odmowa od ręki
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

        // Dach panoramiczny wyklucza się z relingami -> Walidacja Technologiczna (Fail-fast)
        assertThatThrownBy(() -> specification.addOption(new OptionCode("PANORAMIC_ROOF"), catalog))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("mutually exclusive");

        // Konfiguracja pozostaje nienaruszona (wadliwa opcja nie weszła)
        assertThat(specification.getSelectedOptions()).containsExactly(new OptionCode("ROOF_RAILS"));
    }

    @Test
    void shouldFailFastInBothDirectionsOfExclusionRule() {
        ProductCatalog catalog = createCatalogWithExclusion();
        VehicleSpecification specification = createSpecification(catalog);

        // Reguła działa też "od drugiej strony": najpierw źródło reguły, potem cel
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

        // Klient rezygnuje z jedynej wybranej opcji
        specification.removeOption(new OptionCode("LED_LIGHTS"));

        // Pusta konfiguracja wraca do DRAFT
        assertThat(specification.state()).isEqualTo(SpecificationState.DRAFT);
        assertThat(specification.getSelectedOptions()).isEmpty();
    }

    @Test
    void shouldFinalizeSpecificationAndRegisterDomainEvent() {
        ProductCatalog catalog = createCatalogWithExclusion();
        VehicleSpecification specification = createSpecification(catalog);
        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);

        // Zamknięcie konfiguracji (UC-KON-01)
        specification.finalizeSpecification();

        // Stan FINAL + zdarzenie SpecyfikacjaSkompletowana odłożone w agregacie
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

        // Pustej konfiguracji nie wolno sfinalizować
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

        // Sfinalizowana specyfikacja jest niemutowalna — ani dodanie, ani usunięcie opcji
        assertThatThrownBy(() -> specification.addOption(new OptionCode("PANORAMIC_ROOF"), catalog))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("finalized");
        assertThatThrownBy(() -> specification.removeOption(new OptionCode("LED_LIGHTS")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("finalized");
    }
}
