package salon.catalog.infrastructure.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** Encja JPA cennika produktowego (UC-KON-02). */
@Entity
@Table(name = "product_catalogs")
public class ProductCatalogJpaEntity {

    @Id
    public String id;
    public String modelYear;
    public int version;
    public String state;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "catalog_options", joinColumns = @JoinColumn(name = "catalog_id"))
    public List<CatalogOptionEmbeddable> options = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "catalog_rules", joinColumns = @JoinColumn(name = "catalog_id"))
    public List<CatalogRuleEmbeddable> rules = new ArrayList<>();

    public ProductCatalogJpaEntity() {
    }
}
