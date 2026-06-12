package unit.catalog_context.domainTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import salon.catalog.domain.event.SpecificationCompletedEvent;
import salon.catalog.domain.model.catalog.*;
import salon.catalog.domain.model.specification.RuleViolationException;
import salon.catalog.domain.model.specification.SpecificationState;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.catalog.domain.service.RuleValidationDomainService;
import salon.catalog.infrastructure.mock.InMemoryCatalogRepository;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import static org.assertj.core.api.Assertions.*;

/** UC-KON-01: Budowa specyfikacji pojazdu — domena + serwis dziedzinowy walidacji reguł */
class BuildSpecificationDomainTest {

    private InMemoryCatalogRepository catalogRepository;
    private RuleValidationDomainService ruleValidation;
    private ProductCatalog catalog;

    @BeforeEach
    void setupCatalog() {
        // Cennik: silnik wysokoprężny WYMAGA automatu, automat WYKLUCZA skrzynię manualną
        catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(new CatalogOption(new OptionCode("DIESEL_ENGINE"), Money.of(12000, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("AUTO_GEARBOX"), Money.of(9000, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("MANUAL_GEARBOX"), Money.of(0, "PLN")));
        catalog.addRule(new CatalogRule(
                new OptionCode("DIESEL_ENGINE"), new OptionCode("AUTO_GEARBOX"), RuleType.REQUIRES));
        catalog.addRule(new CatalogRule(
                new OptionCode("AUTO_GEARBOX"), new OptionCode("MANUAL_GEARBOX"), RuleType.EXCLUDES));

        catalogRepository = new InMemoryCatalogRepository();
        catalogRepository.save(catalog);
        ruleValidation = new RuleValidationDomainService(catalogRepository);
    }

    private VehicleSpecification newSpecification() {
        return new VehicleSpecification(SpecificationId.generate(), catalog.getCatalogId());
    }

    @Test
    void shouldBuildValidSpecificationThroughDomainService() {
        VehicleSpecification specification = newSpecification();

        // Serwis dziedzinowy sam pobiera cennik z repozytorium i zleca agregatowi dodanie opcji
        ruleValidation.validateAndAddOption(specification, new OptionCode("DIESEL_ENGINE"));
        ruleValidation.validateAndAddOption(specification, new OptionCode("AUTO_GEARBOX"));

        assertThat(specification.getState()).isEqualTo(SpecificationState.IN_PROGRESS);
        assertThat(specification.getTotalPrice()).isEqualTo(Money.of(21000, "PLN"));
    }

    @Test
    void shouldFailFastOnExclusionRuleViolation() {
        VehicleSpecification specification = newSpecification();
        ruleValidation.validateAndAddOption(specification, new OptionCode("AUTO_GEARBOX"));

        // Walidacja Technologiczna (Fail-fast): manualna skrzynia wyklucza się z automatem
        assertThatThrownBy(() ->
                ruleValidation.validateAndAddOption(specification, new OptionCode("MANUAL_GEARBOX")))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("mutually exclusive");
    }

    @Test
    void shouldBlockFinalizationWhenRequiredOptionIsMissing() {
        VehicleSpecification specification = newSpecification();
        // Wybrano diesla, ale bez wymaganego automatu
        ruleValidation.validateAndAddOption(specification, new OptionCode("DIESEL_ENGINE"));

        // Reguła finalizacji: kompletność wg REQUIRES blokuje zamknięcie konfiguracji
        assertThatThrownBy(() -> ruleValidation.assertComplete(specification))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("requires");
    }

    @Test
    void shouldFinalizeCompleteSpecificationAndRegisterEvent() {
        VehicleSpecification specification = newSpecification();
        ruleValidation.validateAndAddOption(specification, new OptionCode("DIESEL_ENGINE"));
        ruleValidation.validateAndAddOption(specification, new OptionCode("AUTO_GEARBOX"));

        // Kompletna konfiguracja przechodzi ocenę i daje się sfinalizować
        ruleValidation.assertComplete(specification);
        specification.finalizeSpecification();

        assertThat(specification.getState()).isEqualTo(SpecificationState.FINAL);
        assertThat(specification.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(SpecificationCompletedEvent.class);
    }

    @Test
    void shouldFailWhenCatalogReferencedBySpecificationDoesNotExist() {
        // Specyfikacja wskazuje na cennik, którego nie ma w repozytorium
        VehicleSpecification orphan =
                new VehicleSpecification(SpecificationId.generate(), new CatalogId("CAT-GHOST"));

        assertThatThrownBy(() ->
                ruleValidation.validateAndAddOption(orphan, new OptionCode("DIESEL_ENGINE")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Catalog not found");
    }
}
