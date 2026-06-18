package salon.catalog.infrastructure.out.persistence;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/** Opcja cennika jako element kolekcji agregatu ProductCatalog. */
@Embeddable
public class CatalogOptionEmbeddable {

    public String code;
    public BigDecimal price;
    public String currency;

    public CatalogOptionEmbeddable() {
    }
}
