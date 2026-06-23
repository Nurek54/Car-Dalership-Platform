package unit.catalog_and_configurator_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.SpecificationState;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** UC-KON-01: Agregat VehicleSpecification (koszyk konfiguracyjny użytkownika). */
class VehicleSpecificationTest {

    private final ProductCatalogFactory catalogFactory = new ProductCatalogFactory();
    private final VehicleSpecificationFactory specFactory = new VehicleSpecificationFactory();

    private ProductCatalog catalogWith(String... codesAndPrices) {
        // Buduje cennik z par kod/cena (np. "B2", "10000", "C1", "5000")
        java.util.List<CatalogOption> options = new java.util.ArrayList<>();
        for (int i = 0; i < codesAndPrices.length; i += 2) {
            options.add(new CatalogOption(OptionCode.of(codesAndPrices[i]),
                    Money.of(new BigDecimal(codesAndPrices[i + 1]), "PLN")));
        }
        return catalogFactory.createNew(ModelYear.of(2025), options, List.of());
    }

    @Test
    void shouldStartAsEmptyDraft() {
        ProductCatalog catalog = catalogWith("B2", "10000");
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");

        // Świeża specyfikacja jest pustym DRAFT-em z zerową ceną
        assertThat(spec.state()).isEqualTo(SpecificationState.DRAFT);
        assertThat(spec.totalPrice()).isEqualTo(Money.zero("PLN"));
        assertThat(spec.optionsPicked()).isEmpty();
    }

    @Test
    void shouldAddOptionsAndRecalculateTotalPrice() {
        ProductCatalog catalog = catalogWith("B2", "10000", "C1", "5000");
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");

        // Pierwsze dodanie opcji przenosi agregat do stanu IN_PROGRESS
        spec.addOption(OptionCode.of("B2"), catalog);
        assertThat(spec.state()).isEqualTo(SpecificationState.IN_PROGRESS);
        assertThat(spec.totalPrice()).isEqualTo(Money.of(new BigDecimal("10000"), "PLN"));

        // Cena łączna = suma cen bazowych wybranych opcji
        spec.addOption(OptionCode.of("C1"), catalog);
        assertThat(spec.totalPrice()).isEqualTo(Money.of(new BigDecimal("15000"), "PLN"));
        assertThat(spec.optionsPicked()).containsExactly(OptionCode.of("B2"), OptionCode.of("C1"));
    }

    @Test
    void shouldRemoveOptionAndRecalculatePrice() {
        ProductCatalog catalog = catalogWith("B2", "10000", "C1", "5000");
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");
        spec.addOption(OptionCode.of("B2"), catalog);
        spec.addOption(OptionCode.of("C1"), catalog);

        // Usunięcie opcji przelicza cenę w dół
        spec.removeOption(OptionCode.of("C1"), catalog);
        assertThat(spec.totalPrice()).isEqualTo(Money.of(new BigDecimal("10000"), "PLN"));
        assertThat(spec.optionsPicked()).containsExactly(OptionCode.of("B2"));
    }

    @Test
    void shouldRejectOptionThatIsNotInCatalog() {
        ProductCatalog catalog = catalogWith("B2", "10000");
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");

        // Do specyfikacji można dodawać wyłącznie opcje istniejące w cenniku
        assertThatThrownBy(() -> spec.addOption(OptionCode.of("X9"), catalog))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectOperationWithMismatchedCatalog() {
        ProductCatalog catalog = catalogWith("B2", "10000");
        ProductCatalog otherCatalog = catalogWith("B2", "10000"); // inny CatalogId
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");

        // Cennik przekazany do komendy musi być tym, na którym oparto specyfikację
        assertThatThrownBy(() -> spec.addOption(OptionCode.of("B2"), otherCatalog))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotFinalizeEmptySpecification() {
        ProductCatalog catalog = catalogWith("B2", "10000");
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");

        // Finalizacja wymaga niepustego zestawu opcji
        assertThatThrownBy(spec::finalizeSpecification)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldBecomeImmutableAfterFinalization() {
        ProductCatalog catalog = catalogWith("B2", "10000");
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");
        spec.addOption(OptionCode.of("B2"), catalog);

        // Finalizacja przenosi agregat do końcowego stanu FINAL
        spec.finalizeSpecification();
        assertThat(spec.state()).isEqualTo(SpecificationState.FINAL);

        // Specyfikacja w stanie FINAL jest niemutowalna
        assertThatThrownBy(() -> spec.addOption(OptionCode.of("B2"), catalog))
                .isInstanceOf(IllegalStateException.class);
    }
}
