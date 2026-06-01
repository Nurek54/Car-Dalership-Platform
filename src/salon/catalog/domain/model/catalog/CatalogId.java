package salon.catalog.domain.model.catalog;

import java.util.UUID;

public record CatalogId(String value) {

    public CatalogId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CatalogId must not be blank.");
        }
    }

    public static CatalogId generate() {
        return new CatalogId("CAT-" + UUID.randomUUID());
    }
}
