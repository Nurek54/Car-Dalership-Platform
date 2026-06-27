package unit.catalog_and_configurator_context.factories;

import org.junit.jupiter.api.Test;
import salon.catalog.application.domain.exception.CatalogValidationException;
import salon.catalog.application.domain.model.catalog.CatalogId;
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

/** Fabryka agregatu ProductCatalog — jedyna ścieżka tworzenia poprawnego cennika. */
class ProductCatalogFactoryTest {

    private final ProductCatalogFactory factory = new ProductCatalogFactory();

    private CatalogOption option(String code, String price) {
        return new CatalogOption(OptionCode.of(code), Money.of(new BigDecimal(price), "PLN"));
    }

    @Test
    void shouldCreateNewActiveCatalogAtVersionOne() {
        // Poprawne dane wejściowe dla nowego cennika
        ProductCatalog catalog = factory.createNew(
                ModelYear.of(2025), List.of(option("B2", "10000")), List.of());

        assertThat(catalog.id()).isNotNull(); // fabryka sama generuje globalny identyfikator
        assertThat(catalog.state()).isEqualTo(CatalogState.ACTIVE);
        assertThat(catalog.version()).isEqualTo(1);
    }

    @Test
    void shouldCreateNextVersionAsActive() {
        // UC-KON-02: nowy cennik zastępuje bieżący, z wyższym numerem wersji
        ProductCatalog catalog = factory.createNextVersion(
                ModelYear.of(2025), 3, List.of(option("B2", "10000")), List.of());

        assertThat(catalog.version()).isEqualTo(3);
        assertThat(catalog.state()).isEqualTo(CatalogState.ACTIVE);
    }

    @Test
    void shouldReconstituteWithoutBusinessValidation() {
        // Odtworzenie z bazy nie waliduje reguł — dane pochodzą z zaufanego źródła
        ProductCatalog catalog = factory.reconstitute(
                CatalogId.generate(), ModelYear.of(2024), 7, CatalogState.ARCHIVED,
                List.of(option("B2", "10000")), List.of());

        assertThat(catalog.version()).isEqualTo(7);
        assertThat(catalog.state()).isEqualTo(CatalogState.ARCHIVED);
    }

    @Test
    void shouldRejectEmptyCatalog() {
        // Niezmiennik: katalog musi zawierać co najmniej jedną opcję
        assertThatThrownBy(() -> factory.createNew(ModelYear.of(2025), List.of(), List.of()))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("at least one option");
    }

    @Test
    void shouldRejectDuplicateOptionCodes() {
        // Niezmiennik: kody opcji w obrębie katalogu są unikalne
        assertThatThrownBy(() -> factory.createNew(
                ModelYear.of(2025), List.of(option("B2", "10000"), option("B2", "9999")), List.of()))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("Duplicate option code");
    }

    @Test
    void shouldRejectRuleReferencingUnknownOption() {
        // Niezmiennik: reguły odwołują się wyłącznie do opcji istniejących w katalogu
        CatalogRule rule = new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.EXCLUDES);
        assertThatThrownBy(() -> factory.createNew(
                ModelYear.of(2025), List.of(option("B2", "10000")), List.of(rule)))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("non-existent option");
    }
}
