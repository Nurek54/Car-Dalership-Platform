package integration.driven_adapters.database_adapter.catalog;

import salon.catalog.infrastructure.persistence.VehicleSpecificationDatabaseAdapter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Import;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.shared.model.SpecificationId;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.specification.SpecificationState;
import salon.shared.model.Money;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@EntityScan("salon")
@EnableJpaRepositories("salon")
@DataJpaTest
@Import(VehicleSpecificationDatabaseAdapter.class)
class VehicleSpecificationDatabaseAdapterTest {

    @Autowired
    private VehicleSpecificationDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: Kolekcje i powiązania
    @Test
    void shouldSaveAndRetrieveVehicleSpecificationWithAllOptionsMapped() {
        // Arrange
        SpecificationId specId = new SpecificationId("SPEC-999");
        CatalogId catalogId = new CatalogId("CAT-2026-V1");
        VehicleSpecification specification = new VehicleSpecification(specId, catalogId);

        // Cennik z dostępnymi opcjami — agregat waliduje obecność opcji w cenniku przy dodawaniu.
        ProductCatalog catalog = ProductCatalog.createActive("2026");
        catalog.addOption(new CatalogOption(new OptionCode("ENGINE_2.0"), Money.of(15000, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("PAINT_METALLIC_BLACK"), Money.of(3000, "PLN")));

        // Dodajemy opcje wyposażenia (np. silnik i lakier)
        specification.addOption(new OptionCode("ENGINE_2.0"), catalog);
        specification.addOption(new OptionCode("PAINT_METALLIC_BLACK"), catalog);

        // Zmieniamy stan na finalny (gotowy do sprzedaży)
        specification.finalizeSpecification();

        // Act - Zapis w bazie
        adapter.save(specification);

        // Odczyt z bazy
        Optional<VehicleSpecification> retrievedSpec = adapter.findById(specId);

        // Assert
        assertThat(retrievedSpec).isPresent();

        // Sprawdzamy identyfikatory
        VehicleSpecification spec = retrievedSpec.get();
        assertThat(spec.getId()).isEqualTo(specId);
        assertThat(spec.getCatalogId()).isEqualTo(catalogId);
        assertThat(spec.getState()).isEqualTo(SpecificationState.FINAL);

        // KLUCZOWE WERYFIKACJE MAPOWANIA: Czy baza poprawnie zapisała i odtworzyła listę opcji?
        assertThat(spec.getSelectedOptions())
                .hasSize(2)
                .extracting(OptionCode::value)
                .containsExactlyInAnyOrder("ENGINE_2.0", "PAINT_METALLIC_BLACK");
    }

    // 2. BRAK DANYCH: Obsługa wyimaginowanej specyfikacji
    @Test
    void shouldReturnEmptyOptionalWhenVehicleSpecificationDoesNotExist() {
        // Act
        Optional<VehicleSpecification> result = adapter.findById(new SpecificationId("SPEC-GHOST"));

        // Assert
        assertThat(result).isEmpty();
    }
}