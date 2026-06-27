package unit.catalog_and_configurator_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.CatalogState;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** UC-KON-01/02: Agregat ProductCatalog (Matryca Produkcyjna / Cennik). */
class ProductCatalogTest {

    private final ProductCatalogFactory factory = new ProductCatalogFactory();

    private ProductCatalog sampleCatalog() {
        // Cennik na rocznik 2025 z dwiema opcjami i regułą wykluczenia
        CatalogOption engine = new CatalogOption(OptionCode.of("B2"), Money.of(new BigDecimal("10000"), "PLN"));
        CatalogOption gearbox = new CatalogOption(OptionCode.of("C1"), Money.of(new BigDecimal("5000"), "PLN"));
        CatalogRule excludes = new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.EXCLUDES);
        return factory.createNew(ModelYear.of(2025), List.of(engine, gearbox), List.of(excludes));
    }

    @Test
    void shouldBeCreatedAsActiveFirstVersion() {
        // Fabryka tworzy cennik w stanie ACTIVE w wersji 1
        ProductCatalog catalog = sampleCatalog();

        assertThat(catalog.state()).isEqualTo(CatalogState.ACTIVE);
        assertThat(catalog.version()).isEqualTo(1);
        assertThat(catalog.modelYear()).isEqualTo(ModelYear.of(2025));
    }

    @Test
    void shouldAnswerOptionQueries() {
        ProductCatalog catalog = sampleCatalog();

        // Opcja istniejąca w cenniku jest rozpoznawana, a jej cena bazowa zwracana
        assertThat(catalog.containsOption(OptionCode.of("B2"))).isTrue();
        assertThat(catalog.containsOption(OptionCode.of("X9"))).isFalse();
        assertThat(catalog.priceOf(OptionCode.of("B2"))).isEqualTo(Money.of(new BigDecimal("10000"), "PLN"));
        assertThat(catalog.currencyCode()).isEqualTo("PLN");
    }

    @Test
    void shouldThrowWhenAskingPriceOfUnknownOption() {
        ProductCatalog catalog = sampleCatalog();

        // Pytanie o cenę opcji spoza cennika jest naruszeniem niezmiennika
        assertThatThrownBy(() -> catalog.priceOf(OptionCode.of("X9")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldArchiveAndBeIdempotent() {
        ProductCatalog catalog = sampleCatalog();

        // Serwis aplikacyjny archiwizuje stary cennik przy zapisie nowej wersji
        catalog.archive();
        assertThat(catalog.state()).isEqualTo(CatalogState.ARCHIVED);

        // Ponowna archiwizacja jest idempotentna (nie zmienia stanu)
        catalog.archive();
        assertThat(catalog.state()).isEqualTo(CatalogState.ARCHIVED);
    }

    @Test
    void shouldExposeOptionsAndRulesAsReadOnly() {
        ProductCatalog catalog = sampleCatalog();

        // Prawo Demeter — klient nie modyfikuje wnętrza agregatu
        assertThatThrownBy(() -> catalog.options().add(
                new CatalogOption(OptionCode.of("Z1"), Money.of(BigDecimal.ONE, "PLN"))))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> catalog.rules().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
