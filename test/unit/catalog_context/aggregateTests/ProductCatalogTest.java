package unit.catalog_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.catalog.domain.event.CatalogUpdatedEvent;
import salon.catalog.domain.model.catalog.*;
import salon.shared.model.Money;

import static org.assertj.core.api.Assertions.*;

class ProductCatalogTest {

    private CatalogOption ledLights() {
        return new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(4500, "PLN"));
    }

    @Test
    void shouldCreateActiveCatalogAndRegisterCatalogUpdatedEvent() {
        // Publication of a new price list version for the model year (UC-KON-02)
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");

        // The price list starts as ACTIVE at version 1
        assertThat(catalog.state()).isEqualTo(CatalogState.ACTIVE);
        assertThat(catalog.getVersion()).isEqualTo(1);
        assertThat(catalog.getModelYear()).isEqualTo(new ModelYear("MY_2026"));

        // The CatalogUpdated event is recorded in the aggregate (listened to by, among others, the Sales Context)
        assertThat(catalog.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(CatalogUpdatedEvent.class)
                .satisfies(event -> {
                    CatalogUpdatedEvent e = (CatalogUpdatedEvent) event;
                    assertThat(e.catalogId()).isEqualTo(catalog.getCatalogId().value());
                    assertThat(e.modelYear()).isEqualTo("MY_2026");
                });
    }

    @Test
    void shouldFindOptionByCodeAndExposeItsPrice() {
        // A price list with a single equipment option
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(ledLights());

        // Lookup by code returns the option together with its base price
        assertThat(catalog.findOption(new OptionCode("LED_LIGHTS")))
                .isPresent()
                .hasValueSatisfying(option ->
                        assertThat(option.basePrice()).isEqualTo(Money.of(4500, "PLN")));

        // An unknown code -> no result (Optional.empty), without an exception
        assertThat(catalog.findOption(new OptionCode("V12_ENGINE"))).isEmpty();
    }

    @Test
    void shouldFreezeCatalogAfterArchiving() {
        // Releasing a new version archives the old one (UC-KON-02)
        ProductCatalog catalog = ProductCatalog.createActive("MY_2025");
        catalog.archive();

        assertThat(catalog.state()).isEqualTo(CatalogState.ARCHIVED);

        // An archived price list is "frozen" — it accepts no new options or rules
        assertThatThrownBy(() -> catalog.addOption(ledLights()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ARCHIVED");
        assertThatThrownBy(() -> catalog.addRule(new CatalogRule(
                new OptionCode("A"), new OptionCode("B"), RuleType.EXCLUDES)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ARCHIVED");
    }

    @Test
    void shouldRejectConstructionWithInvalidVersion() {
        // The price list version must be >= 1 — a constructor invariant
        assertThatThrownBy(() -> new ProductCatalog(
                CatalogId.generate(), new ModelYear("MY_2026"), 0, CatalogState.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version");
    }

    @Test
    void shouldReturnDefensiveCopiesOfOptionsAndRules() {
        // Modifying the lists returned outward must not change the aggregate's interior
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(ledLights());

        catalog.getOptions().clear();
        catalog.getRules().clear();

        assertThat(catalog.getOptions()).hasSize(1); // the defensive copy worked
    }
}
