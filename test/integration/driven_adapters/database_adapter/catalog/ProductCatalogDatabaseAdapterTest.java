package integration.driven_adapters.database_adapter.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.catalog.domain.model.ProductCatalog;
import salon.catalog.domain.model.CatalogId;
import salon.catalog.domain.model.ModelYear;
import salon.catalog.domain.model.CatalogState;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ProductCatalogDatabaseAdapter.class)
class ProductCatalogDatabaseAdapterTest {

    @Autowired
    private ProductCatalogDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: Pełny cykl życia (Round-trip)
    @Test
    void shouldSaveAndRetrieveProductCatalogWithCorrectMapping() {
        // Arrange - Tworzymy czysty obiekt dziedziny
        CatalogId catalogId = new CatalogId("CAT-2026-V1");
        ModelYear year = new ModelYear("2026");
        ProductCatalog catalog = new ProductCatalog(catalogId, year);

        // Aktywujemy cennik, co zmienia jego stan wewnętrzny
        catalog.activate();

        // Act - Adapter mapuje obiekt na encję JPA i zapisuje w bazie H2
        adapter.save(catalog);

        // Odczytujemy z bazy, aby wymusić mapowanie z encji JPA z powrotem na obiekt dziedziny
        Optional<ProductCatalog> retrievedCatalog = adapter.findById(catalogId);

        // Assert - Weryfikujemy, czy dane nie uległy zniekształceniu podczas transformacji
        assertThat(retrievedCatalog).isPresent();
        assertThat(retrievedCatalog.get().getId()).isEqualTo(catalogId);
        assertThat(retrievedCatalog.get().getModelYear().value()).isEqualTo("2026");
        assertThat(retrievedCatalog.get().getState()).isEqualTo(CatalogState.ACTIVE);
    }

    // 2. BRAK DANYCH: Bezpieczna obsługa nieistniejących rekordów
    @Test
    void shouldReturnEmptyOptionalWhenProductCatalogDoesNotExist() {
        // Act
        Optional<ProductCatalog> result = adapter.findById(new CatalogId("CAT-GHOST"));

        // Assert
        assertThat(result).isEmpty();
    }
}