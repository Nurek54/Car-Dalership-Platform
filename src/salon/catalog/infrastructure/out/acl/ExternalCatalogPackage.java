package salon.catalog.infrastructure.out.acl;

import java.util.List;

public record ExternalCatalogPackage(int year,
                                     List<ExternalItem> priceList,
                                     List<ExternalRestriction> restrictions) {

    
    public record ExternalItem(String featureCode, long priceMinorUnits, String currencyIso) {
    }

    
    public record ExternalRestriction(String fromFeature, String toFeature, String kind) {
    }
}
