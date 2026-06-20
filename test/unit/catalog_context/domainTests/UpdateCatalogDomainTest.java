package unit.catalog_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.catalog.domain.event.CatalogUpdatedEvent;
import salon.catalog.domain.model.catalog.*;
import salon.shared.model.Money;

import static org.assertj.core.api.Assertions.*;

/** UC-KON-02: Automatyczna aktualizacja cennika — reguły domenowe cyklu życia cennika */
class UpdateCatalogDomainTest {

    @Test
    void shouldPublishNewActiveVersionWithCatalogUpdatedEvent() {
        // Publikacja nowej wersji cennika rozsyła w świat zdarzenie CatalogUpdated
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");

        assertThat(catalog.state()).isEqualTo(CatalogState.ACTIVE);
        assertThat(catalog.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(CatalogUpdatedEvent.class);
    }

    @Test
    void shouldPullDomainEventsOnlyOnce() {
        // Wzorzec "collect & pull": warstwa aplikacji ściąga zdarzenia jednorazowo
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");

        assertThat(catalog.pullDomainEvents()).hasSize(1);
        assertThat(catalog.pullDomainEvents()).isEmpty(); // lista wyczyszczona po pierwszym pull
    }

    @Test
    void shouldKeepArchivedVersionImmutable() {
        // Wydanie nowej wersji archiwizuje starą — stare wersje są niemutowalne
        ProductCatalog oldVersion = ProductCatalog.createActive("MY_2025");
        oldVersion.addOption(new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(4500, "PLN")));

        oldVersion.archive();

        // Zamrożony cennik nadal udostępnia swoje opcje do odczytu...
        assertThat(oldVersion.findOption(new OptionCode("LED_LIGHTS"))).isPresent();

        // ...ale odrzuca każdą próbę modyfikacji
        assertThatThrownBy(() -> oldVersion.addOption(
                new CatalogOption(new OptionCode("TOW_HOOK"), Money.of(2000, "PLN"))))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> oldVersion.addRule(new CatalogRule(
                new OptionCode("A"), new OptionCode("B"), RuleType.REQUIRES)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectBlankModelYear() {
        // Rocznik cennika to obowiązkowy Value Object — pusta wartość łamie niezmiennik
        assertThatThrownBy(() -> ProductCatalog.createActive(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ModelYear");
    }
}
