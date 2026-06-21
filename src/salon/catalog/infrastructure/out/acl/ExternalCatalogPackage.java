package salon.catalog.infrastructure.out.acl;

import java.util.List;

/**
 * FOREIGN data model of the external manufacturer/importer system (Blackbox).
 *
 * It intentionally uses the vendor's naming and structure (the "upstream" context), which differ
 * from the ubiquitous language of the Catalog context. Translation into the local model
 * is performed by {@link ImporterCatalogTranslator} within the ACL layer.
 */
public record ExternalCatalogPackage(int year,
                                     List<ExternalItem> priceList,
                                     List<ExternalRestriction> restrictions) {

    /** A price-list entry in the vendor format (price as cents + ISO currency code). */
    public record ExternalItem(String featureCode, long priceMinorUnits, String currencyIso) {
    }

    /** A combination constraint in the vendor format (kind = "INCOMPATIBLE_WITH" | "DEPENDS_ON"). */
    public record ExternalRestriction(String fromFeature, String toFeature, String kind) {
    }
}
