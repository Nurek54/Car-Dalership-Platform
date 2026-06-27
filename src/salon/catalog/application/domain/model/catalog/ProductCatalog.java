package salon.catalog.application.domain.model.catalog;

import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProductCatalog {

    private final CatalogId id;
    private final ModelYear modelYear;
    private int version;
    private CatalogState state;
    private final List<CatalogOption> options;
    private final List<CatalogRule> rules;

    
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

    
    public void archive() {
        if (state == CatalogState.ARCHIVED) {
            return;
        }
        this.state = CatalogState.ARCHIVED;
    }

    
    public boolean containsOption(OptionCode code) {
        return findOption(code).isPresent();
    }

    
    public Money priceOf(OptionCode code) {
        return findOption(code)
                .map(CatalogOption::basePrice)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Option " + code + " does not exist in catalog " + id));
    }

    private Optional<CatalogOption> findOption(OptionCode code) {
        return options.stream().filter(o -> o.code().equals(code)).findFirst();
    }

    
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
