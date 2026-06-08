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
 * Aggregate Root: konfiguracja pojazdu budowana przez klienta/Handlowca (UC-KAT-01).
 *
 * Reguła kluczowa (Walidacja Technologiczna, Fail-fast): przy KAŻDYM dodaniu opcji sprawdzamy
 * reguły cennika. Jeśli nowa opcja wyklucza się z już wybraną -> RuleViolationException od ręki.
 *
 * Cennik przekazujemy jako argument (addOption(code, catalog)) — agregat sam nie sięga do bazy
 * (to robi warstwa aplikacji/serwis dziedzinowy), dzięki czemu domena pozostaje czysta.
 */
public class VehicleSpecification extends AbstractAggregateRoot {

    private final SpecificationId id;
    private final CatalogId catalogId;
    private final List<OptionCode> selectedOptions;
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
        this.selectedOptions = new ArrayList<>();
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
        if (this.state != SpecificationState.DRAFT) {
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

        this.selectedOptions.add(newOption);
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
            if (rule.sourceCode().equals(newOption) && this.selectedOptions.contains(rule.targetCode())) {
                throw new RuleViolationException("Option " + newOption.value()
                        + " is mutually exclusive with " + rule.targetCode().value());
            }
            // Reguła odwrotna: już wybrana opcja wyklucza newOption.
            if (rule.targetCode().equals(newOption) && this.selectedOptions.contains(rule.sourceCode())) {
                throw new RuleViolationException("Option " + newOption.value()
                        + " is mutually exclusive with " + rule.sourceCode().value());
            }
        }
    }

    /**
     * Ścieżka koordynowana przez RuleValidationDomainService (UC-KAT-01).
     * Serwis dziedzinowy pobrał już cennik z repozytorium i sam zweryfikował reguły, dlatego agregat
     * jedynie rejestruje wybraną opcję — bez ponownej kontroli obecności opcji w cenniku.
     * Dublety pomijamy (idempotencja); stan musi pozostać roboczy (DRAFT).
     */
    public void applyValidatedOption(OptionCode option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not be null.");
        }
        if (this.state != SpecificationState.DRAFT) {
            throw new IllegalStateException("Cannot modify a finalized specification.");
        }
        if (!this.selectedOptions.contains(option)) {
            this.selectedOptions.add(option);
        }
    }

    public void removeOption(OptionCode option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not be null.");
        }
        if (this.state != SpecificationState.DRAFT) {
            throw new IllegalStateException("Cannot modify a finalized specification.");
        }
        this.selectedOptions.remove(option);
    }

    // UC-KAT-01: zamknięcie konfiguracji. Wymagamy co najmniej jednej wybranej opcji.
    // Po skompletowaniu agregat ogłasza światu, że specyfikacja jest gotowa do sprzedaży
    // (zdarzenie, na które czeka Kontekst Sprzedaży).
    public void finalizeSpecification() {
        if (this.selectedOptions.isEmpty()) {
            throw new IllegalStateException("Specification must have at least one option to be finalized.");
        }
        this.state = SpecificationState.READY_FOR_SALES;
        registerEvent(new SpecificationCompletedEvent(
                UUID.randomUUID(), this.id.value(), this.catalogId.value(), Instant.now()));
    }

    public List<OptionCode> getSelectedOptions() {
        return new ArrayList<>(this.selectedOptions); // kopia obronna
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
