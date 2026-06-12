package salon.catalog.domain.model.specification;

import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.CatalogRule;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.catalog.RuleType;
import salon.catalog.domain.event.SpecificationCompletedEvent;
import salon.shared.event.AbstractAggregateRoot;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Root: konfiguracja pojazdu budowana przez klienta/Handlowca (UC-KON-01).
 *
 * Reguła kluczowa (Walidacja Technologiczna, Fail-fast): przy KAŻDYM dodaniu opcji sprawdzamy
 * reguły cennika. Jeśli nowa opcja wyklucza się z już wybraną -> RuleViolationException od ręki.
 *
 * Cennik przekazujemy jako argument (addOption(code, catalog)) — agregat sam nie sięga do bazy
 * (to robi warstwa aplikacji/serwis dziedzinowy), dzięki czemu domena pozostaje czysta.
 *
 * Cykl życia: DRAFT -> IN_PROGRESS (po pierwszej opcji) -> FINAL (po skompletowaniu).
 */
public class VehicleSpecification extends AbstractAggregateRoot {

    private final SpecificationId id;
    private final CatalogId catalogId;
    private final List<OptionCode> optionsPicked;
    private Money totalPrice;            // null, dopóki nie wybierzemy pierwszej płatnej opcji
    private SpecificationState state;

    public VehicleSpecification(SpecificationId id, CatalogId catalogId) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (catalogId == null) {
            throw new IllegalArgumentException("catalogId must not be null.");
        }
        this.id = id;
        this.catalogId = catalogId;
        this.optionsPicked = new ArrayList<>();
        this.totalPrice = null;
        this.state = SpecificationState.DRAFT;
    }

    public void addOption(OptionCode newOption, ProductCatalog catalog) {
        if (newOption == null) {
            throw new IllegalArgumentException("newOption must not be null.");
        }
        if (catalog == null) {
            throw new IllegalArgumentException("catalog must not be null.");
        }
        if (this.state == SpecificationState.FINAL) {
            throw new IllegalStateException("Cannot modify a finalized specification.");
        }

        // Opcja musi istnieć w cenniku.
        Optional<CatalogOption> catalogOption = catalog.findOption(newOption);
        if (catalogOption.isEmpty()) {
            throw new IllegalArgumentException(
                    "Option " + newOption.value() + " is not available in the catalog.");
        }

        // Fail-fast: sprawdzamy reguły wykluczeń względem już wybranych opcji.
        checkExclusions(newOption, catalog);

        this.optionsPicked.add(newOption);
        this.state = SpecificationState.IN_PROGRESS;
        Money price = catalogOption.get().basePrice();
        if (this.totalPrice == null) {
            this.totalPrice = price;
        } else {
            this.totalPrice = this.totalPrice.add(price);
        }
    }

    private void checkExclusions(OptionCode newOption, ProductCatalog catalog) {
        List<CatalogRule> rules = catalog.getRules();
        for (int i = 0; i < rules.size(); i++) {
            CatalogRule rule = rules.get(i);
            if (rule.type() != RuleType.EXCLUDES) {
                continue;
            }
            // Reguła: newOption wyklucza coś, co już mamy wybrane.
            if (rule.sourceCode().equals(newOption) && this.optionsPicked.contains(rule.targetCode())) {
                throw new RuleViolationException("Option " + newOption.value()
                        + " is mutually exclusive with " + rule.targetCode().value());
            }
            // Reguła odwrotna: już wybrana opcja wyklucza newOption.
            if (rule.targetCode().equals(newOption) && this.optionsPicked.contains(rule.sourceCode())) {
                throw new RuleViolationException("Option " + newOption.value()
                        + " is mutually exclusive with " + rule.sourceCode().value());
            }
        }
    }

    public void removeOption(OptionCode option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not be null.");
        }
        if (this.state == SpecificationState.FINAL) {
            throw new IllegalStateException("Cannot modify a finalized specification.");
        }
        this.optionsPicked.remove(option);
        if (this.optionsPicked.isEmpty()) {
            this.state = SpecificationState.DRAFT;
        }
    }

    // UC-KON-01: zamknięcie konfiguracji. Wymagamy co najmniej jednej wybranej opcji.
    // Po skompletowaniu agregat ogłasza światu, że specyfikacja jest gotowa do sprzedaży
    // (zdarzenie, na które czeka Kontekst Sprzedaży).
    public void finalizeSpecification() {
        if (this.optionsPicked.isEmpty()) {
            throw new IllegalStateException("Specification must have at least one option to be finalized.");
        }
        this.state = SpecificationState.FINAL;
        registerEvent(new SpecificationCompletedEvent(
                UUID.randomUUID(), this.id.value(), this.catalogId.value(),
                this.optionsPicked.stream().map(OptionCode::value).toList(), Instant.now()));
    }

    public List<OptionCode> getSelectedOptions() {
        return new ArrayList<>(this.optionsPicked); // kopia obronna
    }

    public SpecificationId getId() {
        return this.id;
    }

    public CatalogId getCatalogId() {
        return this.catalogId;
    }

    public Money getTotalPrice() {
        return this.totalPrice;
    }

    public SpecificationState getState() {
        return this.state;
    }
}
