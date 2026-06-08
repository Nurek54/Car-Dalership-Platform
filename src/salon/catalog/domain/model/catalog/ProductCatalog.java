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
 * Cykl życia: SCHEDULED -> ACTIVE -> ARCHIVED. Po archiwizacji cennik jest "zamrożony" —
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
     * Konstruktor skrócony (id + rocznik): tworzy cennik ZAPLANOWANY (SCHEDULED) w wersji 1.
     * Wygodny tam, gdzie chcemy najpierw zbudować cennik, a dopiero potem go aktywować
     * (activate() ogłasza wtedy publikację nowej wersji).
     */
    public ProductCatalog(CatalogId id, ModelYear modelYear) {
        this(id, modelYear, 1, CatalogState.SCHEDULED);
    }

    // Fabryka: nowy, aktywny cennik dla danego rocznika.
    public static ProductCatalog createActive(String modelYear) {
        return new ProductCatalog(CatalogId.generate(), new ModelYear(modelYear), 1, CatalogState.ACTIVE);
    }

    // Fabryka: cennik zaplanowany (wejdzie w życie później) — aktywowany przez Cron.
    public static ProductCatalog createScheduled(String modelYear) {
        return new ProductCatalog(CatalogId.generate(), new ModelYear(modelYear), 1, CatalogState.SCHEDULED);
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

    // Cron (CatalogActivationCronJobAdapter, UC-KAT-02): zaplanowany cennik staje się aktywny.
    // Uruchomienie cennika rozsyła w świat informację o publikacji nowej wersji.
    public void activate() {
        if (this.state != CatalogState.SCHEDULED) {
            throw new IllegalStateException("Only a SCHEDULED catalog can be activated, was: " + this.state);
        }
        this.state = CatalogState.ACTIVE;
        registerEvent(new CatalogVersionPublishedEvent(
                UUID.randomUUID(), this.id.value(), this.modelYear.value(), Instant.now()));
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
