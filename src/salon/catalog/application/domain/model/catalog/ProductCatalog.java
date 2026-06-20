package salon.catalog.application.domain.model.catalog;

import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * KORZEŃ AGREGATU – Matryca Produkcyjna / Cennik.
 *
 * Kompletny zbiór opcji (CatalogOption) i reguł zależności (CatalogRule) przypisany
 * do danego rocznika modelowego. Korzeń jest jedynym punktem dostępu do wnętrza
 * agregatu (prawo Demeter) i odpowiada za sprawdzanie niezmienników.
 *
 * Niezmienniki wymuszane przez agregat:
 *  - katalog ma co najmniej jedną opcję,
 *  - kody opcji są unikatowe,
 *  - reguły odwołują się wyłącznie do istniejących w katalogu opcji.
 *
 * Uwaga (Reguła 1): twardy rozdział między słownikiem reguł (ten agregat) a
 * koszykiem konfiguracyjnym użytkownika (VehicleSpecification) chroni system przed
 * zmianą cen w trakcie długotrwałego ofertowania.
 */
public class ProductCatalog {

    private final CatalogId id;
    private final ModelYear modelYear;
    private int version;
    private CatalogState state;
    private final List<CatalogOption> options;
    private final List<CatalogRule> rules;

    /**
     * Konstruktor pakietowy – wywoływany wyłącznie przez {@code ProductCatalogFactory}
     * (tworzenie nie jest odpowiedzialnością agregatu ani jego klienta).
     */
    ProductCatalog(CatalogId id,
                   ModelYear modelYear,
                   int version,
                   CatalogState state,
                   List<CatalogOption> options,
                   List<CatalogRule> rules) {
        this.id = Objects.requireNonNull(id, "id");
        this.modelYear = Objects.requireNonNull(modelYear, "modelYear");
        this.version = version;
        this.state = Objects.requireNonNull(state, "state");
        this.options = new ArrayList<>(Objects.requireNonNull(options, "options"));
        this.rules = new ArrayList<>(Objects.requireNonNull(rules, "rules"));
    }

    /**
     * Polecenie stanowe: archiwizuje katalog (UC-KON-02, krok 4 – poprzednie dane
     * zostają oznaczone jako archiwalne po zapisaniu nowej wersji).
     */
    public void archive() {
        if (state == CatalogState.ARCHIVED) {
            return;
        }
        this.state = CatalogState.ARCHIVED;
    }

    /** Zapytanie: czy podany kod opcji występuje w tym katalogu. */
    public boolean containsOption(OptionCode code) {
        return findOption(code).isPresent();
    }

    /** Zapytanie: cena bazowa opcji (rzuca wyjątek, gdy opcja spoza katalogu). */
    public Money priceOf(OptionCode code) {
        return findOption(code)
                .map(CatalogOption::basePrice)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Opcja " + code + " nie istnieje w katalogu " + id));
    }

    private Optional<CatalogOption> findOption(OptionCode code) {
        return options.stream().filter(o -> o.code().equals(code)).findFirst();
    }

    /** Kod waluty cennika (katalog ma co najmniej jedną opcję – niezmiennik fabryki). */
    public String currencyCode() {
        return options.get(0).basePrice().currency().getCurrencyCode();
    }

    public CatalogId id() {
        return id;
    }

    public ModelYear modelYear() {
        return modelYear;
    }

    public int version() {
        return version;
    }

    public CatalogState state() {
        return state;
    }

    /** Tylko-do-odczytu (prawo Demeter – klient nie modyfikuje wnętrza agregatu). */
    public List<CatalogOption> options() {
        return Collections.unmodifiableList(options);
    }

    public List<CatalogRule> rules() {
        return Collections.unmodifiableList(rules);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCatalog that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
