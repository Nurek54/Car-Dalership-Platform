package unit.catalog_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.catalog.domain.event.CatalogUpdatedEvent;
import salon.catalog.domain.model.catalog.*;
import salon.shared.model.Money;

import static org.assertj.core.api.Assertions.*;

/** UC-KON-02: Automatic price list update — domain rules of the price list life cycle */
class UpdateCatalogDomainTest {

    @Test
    void shouldPublishNewActiveVersionWithCatalogUpdatedEvent() {
        // Publishing a new price list version broadcasts the CatalogUpdated event
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");

        assertThat(catalog.state()).isEqualTo(CatalogState.ACTIVE);
        assertThat(catalog.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(CatalogUpdatedEvent.class);
    }

    @Test
    void shouldPullDomainEventsOnlyOnce() {
        // The "collect & pull" pattern: the application layer pulls the events once
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");

        assertThat(catalog.pullDomainEvents()).hasSize(1);
        assertThat(catalog.pullDomainEvents()).isEmpty(); // lista wyczyszczona po pierwszym pull
    }

    @Test
    void shouldKeepArchivedVersionImmutable() {
        // Releasing a new version archives the old one — old versions are immutable
        ProductCatalog oldVersion = ProductCatalog.createActive("MY_2025");
        oldVersion.addOption(new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(4500, "PLN")));

        oldVersion.archive();

        // A frozen price list still exposes its options for reading...
        assertThat(oldVersion.findOption(new OptionCode("LED_LIGHTS"))).isPresent();

        // ...but rejects every modification attempt
        assertThatThrownBy(() -> oldVersion.addOption(
                new CatalogOption(new OptionCode("TOW_HOOK"), Money.of(2000, "PLN"))))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> oldVersion.addRule(new CatalogRule(
                new OptionCode("A"), new OptionCode("B"), RuleType.REQUIRES)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectBlankModelYear() {
        // The price list model year is a mandatory Value Object — an empty value breaks the invariant
        assertThatThrownBy(() -> ProductCatalog.createActive(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ModelYear");
    }
}
