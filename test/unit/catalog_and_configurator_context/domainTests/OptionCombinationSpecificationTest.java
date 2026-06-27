package unit.catalog_and_configurator_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.policy.OptionCombinationSpecification;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/** Wzorzec Specyfikacja (Specification) — predykat sprawdzający zgodność kombinacji opcji z regułami. */
class OptionCombinationSpecificationTest {

    @Test
    void shouldDetectExclusionViolation() {
        // B2 wyklucza C1
        OptionCombinationSpecification spec = new OptionCombinationSpecification(
                List.of(new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.EXCLUDES)));

        // Zestaw zawiera obie wykluczające się opcje — kombinacja niedozwolona
        assertThat(spec.isSatisfiedBy(Set.of(OptionCode.of("B2"), OptionCode.of("C1")))).isFalse();
        assertThat(spec.violations(Set.of(OptionCode.of("B2"), OptionCode.of("C1")))).hasSize(1);
    }

    @Test
    void shouldDetectMissingRequiredOption() {
        // A1 wymaga B2
        OptionCombinationSpecification spec = new OptionCombinationSpecification(
                List.of(new CatalogRule(OptionCode.of("A1"), OptionCode.of("B2"), RuleType.REQUIRES)));

        // Wybrano A1 bez wymaganego B2 — naruszenie reguły wymagania
        assertThat(spec.isSatisfiedBy(Set.of(OptionCode.of("A1")))).isFalse();

        // Po dodaniu B2 reguła jest spełniona
        assertThat(spec.isSatisfiedBy(Set.of(OptionCode.of("A1"), OptionCode.of("B2")))).isTrue();
    }

    @Test
    void shouldAcceptCombinationWhenSourceNotPicked() {
        // Reguła dotyczy B2, którego nie wybrano — nie obowiązuje
        OptionCombinationSpecification spec = new OptionCombinationSpecification(
                List.of(new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.EXCLUDES)));

        assertThat(spec.isSatisfiedBy(Set.of(OptionCode.of("A1")))).isTrue();
        assertThat(spec.violations(Set.of(OptionCode.of("A1")))).isEmpty();
    }
}
