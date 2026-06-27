package unit.catalog_and_configurator_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.catalog.application.domain.exception.CombinationNotAllowedException;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;
import salon.catalog.application.domain.service.RuleValidationService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Serwis dziedzinowy (bezstanowy) — walidacja reguł wykluczeń/wymagań. */
class RuleValidationServiceTest {

    private final ProductCatalogFactory catalogFactory = new ProductCatalogFactory();
    private final VehicleSpecificationFactory specFactory = new VehicleSpecificationFactory();
    private final RuleValidationService service = new RuleValidationService();

    private CatalogOption option(String code, String price) {
        return new CatalogOption(OptionCode.of(code), Money.of(new BigDecimal(price), "PLN"));
    }

    @Test
    void validateCompleteShouldRejectEmptySpecification() {
        ProductCatalog catalog = catalogFactory.createNew(
                ModelYear.of(2025), List.of(option("B2", "10000")), List.of());
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");

        // Specyfikacja bez żadnej opcji nie może przejść walidacji końcowej
        assertThatThrownBy(() -> service.validateComplete(spec, catalog))
                .isInstanceOf(CombinationNotAllowedException.class);
    }

    @Test
    void validateSelectionShouldAcceptSatisfiedRequiresRule() {
        // Reguła wymagania: B2 REQUIRES A1
        ProductCatalog catalog = catalogFactory.createNew(
                ModelYear.of(2025),
                List.of(option("B2", "10000"), option("A1", "2000")),
                List.of(new CatalogRule(OptionCode.of("B2"), OptionCode.of("A1"), RuleType.REQUIRES)));

        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");
        spec.addOption(OptionCode.of("B2"), catalog);
        spec.addOption(OptionCode.of("A1"), catalog);

        // Spełniona reguła REQUIRES — brak naruszeń
        assertThatCode(() -> service.validateSelection(spec, catalog)).doesNotThrowAnyException();
    }

    @Test
    void validateSelectionShouldRejectExclusionViolation() {
        // Reguła wykluczenia: B2 EXCLUDES C1
        ProductCatalog catalog = catalogFactory.createNew(
                ModelYear.of(2025),
                List.of(option("B2", "10000"), option("C1", "5000")),
                List.of(new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.EXCLUDES)));

        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");
        spec.addOption(OptionCode.of("B2"), catalog);
        spec.addOption(OptionCode.of("C1"), catalog);

        // Naruszona reguła wykluczenia — kombinacja zablokowana
        assertThatThrownBy(() -> service.validateSelection(spec, catalog))
                .isInstanceOf(CombinationNotAllowedException.class);
    }
}
