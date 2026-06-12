package unit.catalog_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.catalog.domain.event.CatalogUpdatedEvent;
import salon.catalog.domain.model.catalog.*;
import salon.shared.model.Money;

import static org.assertj.core.api.Assertions.*;

class ProductCatalogTest {

    private CatalogOption ledLights() {
        return new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(4500, "PLN"));
    }

    @Test
    void shouldCreateActiveCatalogAndRegisterCatalogUpdatedEvent() {
        // Publikacja nowej wersji cennika dla rocznika (UC-KON-02)
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");

        // Cennik startuje jako ACTIVE w wersji 1
        assertThat(catalog.getState()).isEqualTo(CatalogState.ACTIVE);
        assertThat(catalog.getVersion()).isEqualTo(1);
        assertThat(catalog.getModelYear()).isEqualTo(new ModelYear("MY_2026"));

        // Zdarzenie CatalogUpdated jest odłożone w agregacie (nasłuchuje m.in. Kontekst Sprzedaży)
        assertThat(catalog.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(CatalogUpdatedEvent.class)
                .satisfies(event -> {
                    CatalogUpdatedEvent e = (CatalogUpdatedEvent) event;
                    assertThat(e.catalogId()).isEqualTo(catalog.getCatalogId().value());
                    assertThat(e.modelYear()).isEqualTo("MY_2026");
                });
    }

    @Test
    void shouldFindOptionByCodeAndExposeItsPrice() {
        // Cennik z jedną opcją wyposażenia
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(ledLights());

        // Wyszukanie po kodzie zwraca opcję wraz z ceną bazową
        assertThat(catalog.findOption(new OptionCode("LED_LIGHTS")))
                .isPresent()
                .hasValueSatisfying(option ->
                        assertThat(option.basePrice()).isEqualTo(Money.of(4500, "PLN")));

        // Nieznany kod -> brak wyniku (Optional.empty), bez wyjątku
        assertThat(catalog.findOption(new OptionCode("V12_ENGINE"))).isEmpty();
    }

    @Test
    void shouldFreezeCatalogAfterArchiving() {
        // Wydanie nowej wersji archiwizuje starą (UC-KON-02)
        ProductCatalog catalog = ProductCatalog.createActive("MY_2025");
        catalog.archive();

        assertThat(catalog.getState()).isEqualTo(CatalogState.ARCHIVED);

        // Zarchiwizowany cennik jest "zamrożony" — nie przyjmuje nowych opcji ani reguł
        assertThatThrownBy(() -> catalog.addOption(ledLights()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ARCHIVED");
        assertThatThrownBy(() -> catalog.addRule(new CatalogRule(
                new OptionCode("A"), new OptionCode("B"), RuleType.EXCLUDES)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ARCHIVED");
    }

    @Test
    void shouldRejectConstructionWithInvalidVersion() {
        // Wersja cennika musi być >= 1 — niezmiennik konstruktora
        assertThatThrownBy(() -> new ProductCatalog(
                CatalogId.generate(), new ModelYear("MY_2026"), 0, CatalogState.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version");
    }

    @Test
    void shouldReturnDefensiveCopiesOfOptionsAndRules() {
        // Modyfikacja list zwróconych na zewnątrz nie może zmienić wnętrza agregatu
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(ledLights());

        catalog.getOptions().clear();
        catalog.getRules().clear();

        assertThat(catalog.getOptions()).hasSize(1); // kopia obronna zadziałała
    }
}
