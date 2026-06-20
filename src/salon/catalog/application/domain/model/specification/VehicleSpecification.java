package salon.catalog.application.domain.model.specification;

import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * KORZEŃ AGREGATU – koszyk konfiguracyjny użytkownika (specyfikacja pojazdu).
 *
 * Odwołuje się do agregatu ProductCatalog WYŁĄCZNIE przez {@link CatalogId}
 * (Reguła 3 – odwołania przez identyfikator, nie przez referencję). ProductCatalog
 * jest przekazywany jako argument poleceń tylko po to, by odczytać ceny i sprawdzić
 * niezmienniki w jednej transakcji – nie jest trzymany jako pole.
 *
 * Niezmienniki:
 *  - można dobierać tylko opcje istniejące w katalogu,
 *  - cena całkowita = suma cen bazowych wybranych opcji,
 *  - po zatwierdzeniu (FINAL) specyfikacja jest niezmienna.
 */
public class VehicleSpecification {

    private final SpecificationId id;
    private final CatalogId catalogId;
    private Money totalPrice;
    private SpecificationState state;
    private final Set<OptionCode> optionsPicked;

    /** Konstruktor pakietowy – wywoływany przez {@code VehicleSpecificationFactory}. */
    VehicleSpecification(SpecificationId id,
                         CatalogId catalogId,
                         Money totalPrice,
                         SpecificationState state,
                         Set<OptionCode> optionsPicked) {
        this.id = Objects.requireNonNull(id, "id");
        this.catalogId = Objects.requireNonNull(catalogId, "catalogId");
        this.totalPrice = Objects.requireNonNull(totalPrice, "totalPrice");
        this.state = Objects.requireNonNull(state, "state");
        this.optionsPicked = new LinkedHashSet<>(Objects.requireNonNull(optionsPicked, "optionsPicked"));
    }

    /**
     * Polecenie stanowe: dobiera opcję do specyfikacji (UC-KON-01, kroki 2–3).
     * Sprawdza niezmiennik „opcja musi pochodzić z katalogu” i przelicza cenę całkowitą.
     * Reguły wykluczające/wymagające (EXCLUDES/REQUIRES) waliduje RuleValidationService
     * przed wywołaniem tego polecenia – walidacja całego stanu nie jest odpowiedzialnością encji.
     */
    public void addOption(OptionCode code, ProductCatalog catalog) {
        ensureNotFinal();
        requireSameCatalog(catalog);
        if (!catalog.containsOption(code)) {
            throw new IllegalArgumentException(
                    "Opcja " + code + " nie należy do katalogu " + catalogId);
        }
        optionsPicked.add(code);
        recalculateTotalPrice(catalog);
        markInProgress();
    }

    /** Polecenie stanowe: usuwa wcześniej dobraną opcję i przelicza cenę. */
    public void removeOption(OptionCode code, ProductCatalog catalog) {
        ensureNotFinal();
        requireSameCatalog(catalog);
        optionsPicked.remove(code);
        recalculateTotalPrice(catalog);
        markInProgress();
    }

    /**
     * Polecenie stanowe: zatwierdza specyfikację (UC-KON-01, krok 6–7).
     * Weryfikuje ostateczną kompletność (co najmniej jedna opcja) i przełącza stan na FINAL.
     * Spójność reguł powinna zostać potwierdzona przez RuleValidationService przed finalizacją.
     */
    public void finalizeSpecification() {
        ensureNotFinal();
        if (optionsPicked.isEmpty()) {
            throw new IllegalStateException("Nie można zatwierdzić pustej specyfikacji " + id);
        }
        this.state = SpecificationState.FINAL;
    }

    private void recalculateTotalPrice(ProductCatalog catalog) {
        Money sum = Money.zero(totalPrice.currency().getCurrencyCode());
        for (OptionCode code : optionsPicked) {
            sum = sum.add(catalog.priceOf(code));
        }
        this.totalPrice = sum;
    }

    private void ensureNotFinal() {
        if (state == SpecificationState.FINAL) {
            throw new IllegalStateException("Specyfikacja " + id + " jest już zatwierdzona (FINAL)");
        }
    }

    private void markInProgress() {
        if (state == SpecificationState.DRAFT) {
            state = SpecificationState.IN_PROGRESS;
        }
    }

    private void requireSameCatalog(ProductCatalog catalog) {
        if (!catalog.id().equals(catalogId)) {
            throw new IllegalArgumentException(
                    "Katalog " + catalog.id() + " nie odpowiada specyfikacji opartej o " + catalogId);
        }
    }

    public SpecificationId id() {
        return id;
    }

    public CatalogId catalogId() {
        return catalogId;
    }

    public Money totalPrice() {
        return totalPrice;
    }

    public SpecificationState state() {
        return state;
    }

    public List<OptionCode> optionsPicked() {
        return Collections.unmodifiableList(new ArrayList<>(optionsPicked));
    }

    /** Zwraca kopię zbioru wybranych opcji (dla RuleValidationService / specyfikacji). */
    public Set<OptionCode> pickedAsSet() {
        return new LinkedHashSet<>(optionsPicked);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleSpecification that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
