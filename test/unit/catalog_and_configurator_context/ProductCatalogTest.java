import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ProductCatalogTest {

    @Test
    void shouldArchiveCatalogWhenCreatingNewVersion() {
        // Arrange
        ProductCatalog activeCatalog = ProductCatalog.createActive("MY_2026");

        // Act
        activeCatalog.archive();

        // Assert - Oczekujemy zmiany statusu na ARCHIVED
        assertThat(activeCatalog.getState()).isEqualTo(CatalogState.ARCHIVED);
    }
}