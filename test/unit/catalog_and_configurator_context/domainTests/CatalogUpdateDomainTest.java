package unit.catalog_and_configurator_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.catalog.application.domain.exception.CatalogValidationException;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.CatalogState;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.service.RuleValidationService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** UC-KON-02: Aktualizacja cennika — wersjonowanie i spójność reguł (na agregatach). */
class CatalogUpdateDomainTest {

    private final ProductCatalogFactory factory = new ProductCatalogFactory();
    private final RuleValidationService ruleValidation = new RuleValidationService();

    private CatalogOption option(String code, String price) {
        return new CatalogOption(OptionCode.of(code), Money.of(new BigDecimal(price), "PLN"));
    }

    @Test
    void shouldCreateNextVersionAndArchivePrevious() { // SCENARIUSZ GŁÓWNY
        // Bieżący, aktywny cennik w wersji 1
        ProductCatalog current = factory.createNew(
                ModelYear.of(2025), List.of(option("B2", "10000")), List.of());

        // Nowy pakiet tworzy kolejną wersję, a poprzednia jest archiwizowana
        ProductCatalog next = factory.createNextVersion(
                ModelYear.of(2025), current.version() + 1, List.of(option("B2", "11000")), List.of());
        current.archive();

        assertThat(next.version()).isEqualTo(2);
        assertThat(next.state()).isEqualTo(CatalogState.ACTIVE);
        assertThat(current.state()).isEqualTo(CatalogState.ARCHIVED);
    }

    @Test
    void shouldRejectCatalogWithConflictingRules() {   // Scenariusz alternatywny A1
        // Ten sam zestaw opcji nie może być jednocześnie REQUIRES i EXCLUDES
        ProductCatalog conflicting = factory.createNextVersion(
                ModelYear.of(2025), 2,
                List.of(option("B2", "10000"), option("C1", "5000")),
                List.of(
                        new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.EXCLUDES),
                        new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.REQUIRES)));

        // Walidacja logiczna wykrywa sprzeczność reguł
        assertThatThrownBy(() -> ruleValidation.validateCatalogConsistency(conflicting))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("Conflicting rules");
    }
}
