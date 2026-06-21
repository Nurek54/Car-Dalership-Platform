package salon.catalog.infrastructure.out.persistence;

import java.math.BigDecimal;
import java.util.List;

/**
 * Persistent-storage model for the ProductCatalog aggregate (infrastructure-layer DTO).
 *
 * Separated from the domain model – the data model is created AFTER the domain model and does not
 * leak into the inner layers (the bidirectional mapping is done by the adapter).
 */
public record ProductCatalogRecord(String id,
                                   int modelYear,
                                   int version,
                                   String state,
                                   List<OptionRecord> options,
                                   List<RuleRecord> rules) {

    public record OptionRecord(String code, BigDecimal price, String currency) {
    }

    public record RuleRecord(String sourceCode, String targetCode, String type) {
    }
}
