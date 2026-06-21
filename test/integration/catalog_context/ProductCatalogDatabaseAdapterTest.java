package integration.catalog_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.catalog.domain.model.catalog.*;
import salon.catalog.infrastructure.persistence.ProductCatalogDatabaseAdapter;
import salon.shared.model.Money;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(ProductCatalogDatabaseAdapter.class)
class ProductCatalogDatabaseAdapterTest {

    @Autowired private ProductCatalogDatabaseAdapter databaseAdapter;

    @Test
    void shouldSaveAndLoadCatalogWithOptionsAndRulesFromSqlDatabase() {
        // An active price list with options and an exclusion rule
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(4500, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("PANORAMIC_ROOF"), Money.of(8000, "PLN")));
        catalog.addRule(new CatalogRule(
                new OptionCode("PANORAMIC_ROOF"), new OptionCode("ROOF_RAILS"), RuleType.EXCLUDES));

        // Wykonujemy fizyczny zapis i odczyt
        databaseAdapter.save(catalog);
        Optional<ProductCatalog> loaded = databaseAdapter.findById(catalog.getCatalogId());

        // The aggregate is reconstituted from the database in full: header + options + rules
        assertThat(loaded).isPresent();
        ProductCatalog reloaded = loaded.get();
        assertThat(reloaded.getModelYear()).isEqualTo(new ModelYear("MY_2026"));
        assertThat(reloaded.getVersion()).isEqualTo(1);
        assertThat(reloaded.state()).isEqualTo(CatalogState.ACTIVE);
        assertThat(reloaded.getOptions()).hasSize(2);
        assertThat(reloaded.getRules())
                .singleElement()
                .satisfies(rule -> {
                    assertThat(rule.sourceCode()).isEqualTo(new OptionCode("PANORAMIC_ROOF"));
                    assertThat(rule.targetCode()).isEqualTo(new OptionCode("ROOF_RAILS"));
                    assertThat(rule.type()).isEqualTo(RuleType.EXCLUDES);
                });

        // The option's base price comes back from the database without loss of value
        assertThat(reloaded.findOption(new OptionCode("LED_LIGHTS")))
                .isPresent()
                .hasValueSatisfying(option -> {
                    assertThat(option.basePrice().amount()).isEqualByComparingTo("4500");
                    assertThat(option.basePrice().currency()).isEqualTo("PLN");
                });
    }

    @Test
    void shouldPreserveArchivedStateAcrossReload() {
        // The price list is archived (release of a new version, UC-KON-02) and saved
        ProductCatalog catalog = ProductCatalog.createActive("MY_2025");
        catalog.addOption(new CatalogOption(new OptionCode("TOW_HOOK"), Money.of(2000, "PLN")));
        catalog.archive();
        databaseAdapter.save(catalog);

        // After reading from the database the ARCHIVED state is preserved...
        ProductCatalog reloaded = databaseAdapter.findById(catalog.getCatalogId()).orElseThrow();
        assertThat(reloaded.state()).isEqualTo(CatalogState.ARCHIVED);

        // ...and the reconstituted aggregate still enforces the "freezing" of the old version
        assertThatThrownBy(() -> reloaded.addOption(
                new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(4500, "PLN"))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldReturnAllPersistedCatalogVersions() {
        // Two price list versions: the old archived one and the new active one
        ProductCatalog oldVersion = ProductCatalog.createActive("MY_2025");
        oldVersion.archive();
        ProductCatalog newVersion = ProductCatalog.createActive("MY_2026");
        databaseAdapter.save(oldVersion);
        databaseAdapter.save(newVersion);

        // findAll returns the full set of versions from the database
        assertThat(databaseAdapter.findAll())
                .extracting(ProductCatalog::getCatalogId)
                .contains(oldVersion.getCatalogId(), newVersion.getCatalogId());
    }
}
