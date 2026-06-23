package unit.catalog_and_configurator_context.factories;

import org.junit.jupiter.api.Test;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.SpecificationId;
import salon.catalog.application.domain.model.specification.SpecificationState;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/** Fabryka agregatu VehicleSpecification — spójny stan początkowy konfiguracji. */
class VehicleSpecificationFactoryTest {

    private final VehicleSpecificationFactory factory = new VehicleSpecificationFactory();

    @Test
    void shouldCreateDraftInConsistentInitialState() {
        CatalogId catalogId = CatalogId.generate();

        // UC-KON-01, krok 1: otwarcie sesji konfiguratora tworzy roboczą specyfikację
        VehicleSpecification spec = factory.createDraft(catalogId, "PLN");

        assertThat(spec.id()).isNotNull();              // fabryka generuje globalny identyfikator
        assertThat(spec.catalogId()).isEqualTo(catalogId);
        assertThat(spec.state()).isEqualTo(SpecificationState.DRAFT);
        assertThat(spec.totalPrice()).isEqualTo(Money.zero("PLN"));
        assertThat(spec.optionsPicked()).isEmpty();
    }

    @Test
    void shouldReconstituteFromPersistentState() {
        SpecificationId id = SpecificationId.generate();
        CatalogId catalogId = CatalogId.generate();

        // Odtworzenie agregatu z bazy (adapter repozytorium)
        VehicleSpecification spec = factory.reconstitute(
                id, catalogId, Money.of(new BigDecimal("15000"), "PLN"),
                SpecificationState.IN_PROGRESS, Set.of(OptionCode.of("B2"), OptionCode.of("C1")));

        assertThat(spec.id()).isEqualTo(id);
        assertThat(spec.state()).isEqualTo(SpecificationState.IN_PROGRESS);
        assertThat(spec.totalPrice()).isEqualTo(Money.of(new BigDecimal("15000"), "PLN"));
        assertThat(spec.optionsPicked()).containsExactlyInAnyOrder(OptionCode.of("B2"), OptionCode.of("C1"));
    }
}
