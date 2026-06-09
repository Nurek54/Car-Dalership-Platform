package integration.driven_adapters.database_adapter.catalog;

import salon.catalog.infrastructure.persistence.ProductCatalogDatabaseAdapter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Import;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.ModelYear;
import salon.catalog.domain.model.catalog.CatalogState;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@EntityScan("salon")
@EnableJpaRepositories("salon")
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
        // Aktywny cennik w wersji 1 (cykl życia: ACTIVE -> ARCHIVED).
        ProductCatalog catalog = new ProductCatalog(catalogId, year, 1, CatalogState.ACTIVE);

        // Act - Adapter mapuje obiekt na encję JPA i zapisuje w bazie H2
        adapter.save(catalog);

        // Odczytujemy z bazy, aby wymusić mapowanie z encji JPA z powrotem na obiekt dziedziny
        Optional<ProductCatalog> retrievedCatalog = adapter.findById(catalogId);

        // Assert - Weryfikujemy, czy dane nie uległy zniekształceniu podczas transformacji
        assertThat(retrievedCatalog).isPresent();
        assertThat(retrievedCatalog.get().getCatalogId()).isEqualTo(catalogId);
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