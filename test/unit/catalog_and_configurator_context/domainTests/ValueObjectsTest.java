package unit.catalog_and_configurator_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/** Obiekty wartości (Value Objects) — niemutowalność, porównanie po wartości, brak efektów ubocznych. */
class ValueObjectsTest {

    @Test
    void moneyShouldAddAndSubtractWithoutSideEffects() {
        Money a = Money.of(new BigDecimal("10000"), "PLN");
        Money b = Money.of(new BigDecimal("5000"), "PLN");

        // Operacje zwracają nowy obiekt, nie modyfikując bieżącego
        assertThat(a.add(b)).isEqualTo(Money.of(new BigDecimal("15000"), "PLN"));
        assertThat(a.subtract(b)).isEqualTo(Money.of(new BigDecimal("5000"), "PLN"));
        assertThat(a).isEqualTo(Money.of(new BigDecimal("10000"), "PLN")); // 'a' bez zmian
    }

    @Test
    void moneyShouldRejectNegativeAmountAndCurrencyMismatch() {
        // Kwota nie może być ujemna
        assertThatThrownBy(() -> Money.of(new BigDecimal("-1"), "PLN"))
                .isInstanceOf(IllegalArgumentException.class);

        // Nie wolno mieszać walut
        assertThatThrownBy(() -> Money.of(BigDecimal.TEN, "PLN").add(Money.of(BigDecimal.TEN, "EUR")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void optionCodeShouldNormalizeAndCompareByValue() {
        // Kod opcji jest przycinany i zamieniany na wielkie litery
        assertThat(OptionCode.of(" b2 ").value()).isEqualTo("B2");
        assertThat(OptionCode.of("b2")).isEqualTo(OptionCode.of("B2"));

        // Pusty kod jest niedozwolony
        assertThatThrownBy(() -> OptionCode.of("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void modelYearShouldValidateRange() {
        assertThat(ModelYear.of(2025).year()).isEqualTo(2025);

        // Rocznik spoza dozwolonego zakresu jest odrzucany
        assertThatThrownBy(() -> ModelYear.of(1500))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
