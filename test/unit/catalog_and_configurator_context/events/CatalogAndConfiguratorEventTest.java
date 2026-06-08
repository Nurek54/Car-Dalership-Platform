import org.junit.jupiter.api.Test;
import salon.catalog.domain.model.catalog.*;
import salon.catalog.domain.event.CatalogUpdatedEvent;
import salon.catalog.domain.event.SpecificationCompletedEvent;
import salon.catalog.domain.model.specification.SpecificationState;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import static org.assertj.core.api.Assertions.*;

class CatalogAndConfiguratorEventTest {

    @Test
    void shouldEmitCatalogUpdatedEventWhenNewCatalogIsActivated() {
        // Arrange & Act
        // Zapis nowej aktywnej wersji cennika (UC-KON-02) rozsyła informację w świat.
        ProductCatalog catalog = ProductCatalog.createActive("2026");

        // Assert
        assertThat(catalog.getState()).isEqualTo(CatalogState.ACTIVE);

        // Sprawdzamy, czy agregat "zanotował" zdarzenie CatalogUpdated o nowej wersji cennika
        assertThat(catalog.getDomainEvents())
                .hasAtLeastOneElementOfType(CatalogUpdatedEvent.class);
    }

    @Test
    void shouldEmitSpecificationCompletedEventWhenSpecificationIsFinalized() {
        // Arrange
        // Cennik z wymaganą opcją (silnik) — agregat sam waliduje obecność opcji w cenniku.
        ProductCatalog catalog = ProductCatalog.createActive("2026");
        catalog.addOption(new CatalogOption(new OptionCode("ENGINE_2.0"), Money.of(15000, "PLN")));

        VehicleSpecification specification = new VehicleSpecification(
                new SpecificationId("SPEC-001"),
                catalog.getCatalogId()
        );
        specification.addOption(new OptionCode("ENGINE_2.0"), catalog);

        // Act - Klient zatwierdza ostatecznie konfigurację (UC-KON-01)
        specification.finalizeSpecification();

        // Assert
        // Stan musi ulec zmianie na finalny (gotowy do sprzedaży)
        assertThat(specification.getState()).isEqualTo(SpecificationState.FINAL);

        // Krytyczny test: Czy system wyemitował zdarzenie, na które czeka Kontekst Sprzedaży?
        assertThat(specification.getDomainEvents())
                .hasAtLeastOneElementOfType(SpecificationCompletedEvent.class);
    }
}
