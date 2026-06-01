package unit.catalog_and_configurator_context;

import org.junit.jupiter.api.Test;
import salon.catalog.domain.model.catalog.CatalogState;
import salon.catalog.domain.model.catalog.ProductCatalog;

import static org.assertj.core.api.Assertions.*;

class ProductCatalogTest {

    @Test
    void shouldArchiveCatalogWhenCreatingNewVersion() {
        ProductCatalog activeCatalog = ProductCatalog.createActive("MY_2026");

        activeCatalog.archive();

        assertThat(activeCatalog.getState()).isEqualTo(CatalogState.ARCHIVED);
    }
}
