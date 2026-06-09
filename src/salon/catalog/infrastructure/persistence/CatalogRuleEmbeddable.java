package salon.catalog.infrastructure.persistence;

import jakarta.persistence.Embeddable;

/** Reguła zależności opcji jako element kolekcji agregatu ProductCatalog. */
@Embeddable
public class CatalogRuleEmbeddable {

    public String sourceCode;
    public String targetCode;
    public String type;

    public CatalogRuleEmbeddable() {
    }
}
