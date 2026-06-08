package salon.catalog.domain.model.catalog;

import salon.catalog.domain.event.CatalogVersionPublishedEvent;
import salon.shared.event.AbstractAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Root: cennik produktowy (UC-KAT). Trzyma opcje wyposażenia i reguły zależności.
 *
 * Cykl życia: ACTIVE -> ARCHIVED. Po archiwizacji cennik jest "zamrożony" —
 * nie dodajemy do niego opcji ani reguł.
 */
public class ProductCatalog extends AbstractAggregateRoot {

    private final CatalogId id;
    private final ModelYear modelYear;
    private final int version;
    private final List<CatalogOption> options;
    private final List<CatalogRule> rules;
    private CatalogState state;

    public ProductCatalog(CatalogId id, ModelYear modelYear, int version, CatalogState state) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (modelYear == null) {
            throw new IllegalArgumentException("modelYear must not be null.");
        }
        if (version < 1) {
            throw new IllegalArgumentException("version must be >= 1.");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must not be null.");
        }
        this.id = id;
        this.modelYear = modelYear;
        this.version = version;
        this.options = new ArrayList<>();
        this.rules = new ArrayList<>();
        this.state = state;
    }

    /**
     * Fabryka: nowy, aktywny cennik dla danego rocznika (publikacja nowej wersji, WF-KAT).
     * Opublikowanie aktywnego cennika rozsyła w świat informację o nowej wersji —
     * nasłuchuje m.in. Kontekst Sprzedaży, by unieważnić oferty oparte o starsze cenniki.
     */
    public static ProductCatalog createActive(String modelYear) {
        ProductCatalog catalog =
                new ProductCatalog(CatalogId.generate(), new ModelYear(modelYear), 1, CatalogState.ACTIVE);
        catalog.registerEvent(new CatalogVersionPublishedEvent(
                UUID.randomUUID(), catalog.id.value(), catalog.modelYear.value(), Instant.now()));
        return catalog;
    }

    public void addOption(CatalogOption option) {
        if (option == null) {
            throw new IllegalArgumentException("option must not be null.");
        }
        if (this.state == CatalogState.ARCHIVED) {
            throw new IllegalStateException("Cannot modify an ARCHIVED catalog.");
        }
        this.options.add(option);
    }

    public void addRule(CatalogRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("rule must not be null.");
        }
        if (this.state == CatalogState.ARCHIVED) {
            throw new IllegalStateException("Cannot modify an ARCHIVED catalog.");
        }
        this.rules.add(rule);
    }

    // WF-KAT: wydanie nowej wersji archiwizuje starą.
    public void archive() {
        this.state = CatalogState.ARCHIVED;
    }

    // Wyszukanie opcji po kodzie (np. by poznać cenę). Zwykła pętla, bez Streamów.
    public Optional<CatalogOption> findOption(OptionCode code) {
        for (int i = 0; i < this.options.size(); i++) {
            CatalogOption option = this.options.get(i);
            if (option.code().equals(code)) {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    public List<CatalogRule> getRules() {
        return new ArrayList<>(this.rules); // kopia obronna
    }

    public List<CatalogOption> getOptions() {
        return new ArrayList<>(this.options); // kopia obronna
    }

    public CatalogId getCatalogId() {
        return this.id;
    }

    public ModelYear getModelYear() {
        return this.modelYear;
    }

    public int getVersion() {
        return this.version;
    }

    public CatalogState getState() {
        return this.state;
    }
}
