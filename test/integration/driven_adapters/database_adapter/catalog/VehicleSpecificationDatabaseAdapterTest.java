package integration.driven_adapters.database_adapter.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.catalog.domain.model.VehicleSpecification;
import salon.catalog.domain.model.SpecificationId;
import salon.catalog.domain.model.CatalogId;
import salon.catalog.domain.model.OptionCode;
import salon.catalog.domain.model.SpecificationState;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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

        // Dodajemy opcje wyposażenia (np. silnik i lakier)
        specification.addOption(new OptionCode("ENGINE_2.0"));
        specification.addOption(new OptionCode("PAINT_METALLIC_BLACK"));

        // Zmieniamy stan na gotowy do sprzedaży
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
        assertThat(spec.getState()).isEqualTo(SpecificationState.READY_FOR_SALES);

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