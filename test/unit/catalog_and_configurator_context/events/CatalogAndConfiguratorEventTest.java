import org.junit.jupiter.api.Test;
import salon.catalog.domain.model.catalog.*;
import salon.catalog.domain.model.specification.SpecificationState;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.shared.model.SpecificationId;

import static org.assertj.core.api.Assertions.*;

class CatalogAndConfiguratorEventTest {

    @Test
    void shouldEmitCatalogVersionPublishedEventWhenNewCatalogIsActivated() {
        // Arrange
        // Tworzymy nową matrycę produkcyjną na dany rocznik
        ProductCatalog catalog = new ProductCatalog(new CatalogId("CAT-2026"), new ModelYear("2026"));

        // Act - Aktywacja cennika (UC-KAT-02)
        // Zgodnie z DDD, uruchomienie cennika (po pobraniu z API importera) powinno rozesłać informację w świat
        catalog.activate();

        // Assert
        assertThat(catalog.getState()).isEqualTo(CatalogState.ACTIVE);

        // Sprawdzamy, czy agregat "zanotował" zdarzenie o publikacji nowego cennika
        assertThat(catalog.getDomainEvents())
                .hasAtLeastOneElementOfType(CatalogVersionPublishedEvent.class);
    }

    @Test
    void shouldEmitSpecificationCompletedEventWhenSpecificationIsFinalized() {
        // Arrange
        VehicleSpecification specification = new VehicleSpecification(
                new SpecificationId("SPEC-001"),
                new CatalogId("CAT-2026")
        );
        // Symulujemy dodanie wymaganych opcji (np. silnik, skrzynia biegów)
        specification.addOption(new OptionCode("ENGINE_2.0"), /* przekazany cennik */ null);

        // Act - Klient zatwierdza ostatecznie konfigurację (UC-KAT-01)
        specification.finalizeSpecification();

        // Assert
        // Stan musi ulec zmianie na gotowy do sprzedaży
        assertThat(specification.getState()).isEqualTo(SpecificationState.READY_FOR_SALES);

        // Krytyczny test: Czy system wyemitował zdarzenie, na które czeka Kontekst Sprzedaży?
        assertThat(specification.getDomainEvents())
                .hasAtLeastOneElementOfType(SpecificationCompletedEvent.class);
    }
}