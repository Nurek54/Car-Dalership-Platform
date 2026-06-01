package salon.catalog.domain.model.catalog;

import salon.shared.model.Money;

// Encja LOKALNA cennika: opcja wyposażenia + jej cena bazowa.
public record CatalogOption(OptionCode code, Money basePrice) {

    public CatalogOption {
        if (code == null) {
            throw new IllegalArgumentException("Option code must not be null.");
        }
        if (basePrice == null) {
            throw new IllegalArgumentException("Option basePrice must not be null.");
        }
    }
}
