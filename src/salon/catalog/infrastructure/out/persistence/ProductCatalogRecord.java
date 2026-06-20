package salon.catalog.infrastructure.out.persistence;

import java.math.BigDecimal;
import java.util.List;

/**
 * Model trwałego magazynu dla agregatu ProductCatalog (DTO warstwy infrastruktury).
 *
 * Oddzielony od modelu dziedziny – model danych powstaje PO modelu dziedziny i nie
 * wycieka do warstw wewnętrznych (mapowanie dwukierunkowe wykonuje adapter).
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
