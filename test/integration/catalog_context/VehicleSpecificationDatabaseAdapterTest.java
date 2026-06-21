package integration.catalog_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.specification.SpecificationState;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.catalog.infrastructure.persistence.VehicleSpecificationDatabaseAdapter;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(VehicleSpecificationDatabaseAdapter.class)
class VehicleSpecificationDatabaseAdapterTest {

    @Autowired private VehicleSpecificationDatabaseAdapter databaseAdapter;

    // An auxiliary price list (not persisted — the specification adapter does not need it on read)
    private ProductCatalog createCatalog() {
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(4500, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("PANORAMIC_ROOF"), Money.of(8000, "PLN")));
        return catalog;
    }

    @Test
    void shouldSaveAndLoadDraftSpecificationFromSqlDatabase() {
        // A fresh specification (DRAFT, without options and without pricing)
        SpecificationId id = new SpecificationId("SPEC-DB-1");
        VehicleSpecification specification =
                new VehicleSpecification(id, new CatalogId("CAT-1"));

        databaseAdapter.save(specification);
        Optional<VehicleSpecification> loaded = databaseAdapter.findById(id);

        // The aggregate comes back from the database in its initial state
        assertThat(loaded).isPresent();
        assertThat(loaded.get().state()).isEqualTo(SpecificationState.DRAFT);
        assertThat(loaded.get().getCatalogId()).isEqualTo(new CatalogId("CAT-1"));
        assertThat(loaded.get().getSelectedOptions()).isEmpty();
        assertThat(loaded.get().getTotalPrice()).isNull();
    }

    @Test
    void shouldRoundTripPickedOptionsTotalPriceAndState() {
        // A specification under construction: two options selected per the price list
        ProductCatalog catalog = createCatalog();
        SpecificationId id = new SpecificationId("SPEC-DB-2");
        VehicleSpecification specification = new VehicleSpecification(id, catalog.getCatalogId());
        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);
        specification.addOption(new OptionCode("PANORAMIC_ROOF"), catalog);

        databaseAdapter.save(specification);
        VehicleSpecification reloaded = databaseAdapter.findById(id).orElseThrow();

        // The selected options, state and total pricing come back from the database unchanged
        assertThat(reloaded.state()).isEqualTo(SpecificationState.IN_PROGRESS);
        // (without @OrderColumn the database does not guarantee the order of collection elements)
        assertThat(reloaded.getSelectedOptions())
                .containsExactlyInAnyOrder(new OptionCode("LED_LIGHTS"), new OptionCode("PANORAMIC_ROOF"));
        assertThat(reloaded.getTotalPrice().amount()).isEqualByComparingTo("12500");
        assertThat(reloaded.getTotalPrice().currency()).isEqualTo("PLN");
    }

    @Test
    void shouldPreserveFinalStateAndKeepReloadedSpecificationImmutable() {
        // Skompletowana i sfinalizowana konfiguracja (UC-KON-01)
        ProductCatalog catalog = createCatalog();
        SpecificationId id = new SpecificationId("SPEC-DB-3");
        VehicleSpecification specification = new VehicleSpecification(id, catalog.getCatalogId());
        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);
        specification.finalizeSpecification();

        databaseAdapter.save(specification);
        VehicleSpecification reloaded = databaseAdapter.findById(id).orElseThrow();

        // The FINAL state survived the round trip through the database...
        assertThat(reloaded.state()).isEqualTo(SpecificationState.FINAL);

        // ...and the reconstituted aggregate still blocks modifications
        assertThatThrownBy(() -> reloaded.addOption(new OptionCode("PANORAMIC_ROOF"), catalog))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("finalized");
    }

    @Test
    void shouldReturnEmptyWhenSpecificationDoesNotExist() {
        // A query for a non-existent identifier does not end with an exception
        assertThat(databaseAdapter.findById(new SpecificationId("SPEC-GHOST"))).isEmpty();
    }
}
