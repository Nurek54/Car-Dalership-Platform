package salon.catalog.infrastructure.out.acl;

import java.util.List;

/**
 * OBCY model danych zewnętrznego systemu producenta/importera (Blackbox).
 *
 * Celowo używa nazewnictwa i struktury dostawcy (kontekst „na górze”), które różnią
 * się od języka wszechobecnego kontekstu Katalogu. Tłumaczenie na model lokalny
 * realizuje {@link ImporterCatalogTranslator} w ramach warstwy ACL.
 */
public record ExternalCatalogPackage(int year,
                                     List<ExternalItem> priceList,
                                     List<ExternalRestriction> restrictions) {

    /** Pozycja cennika w formacie dostawcy (cena jako grosze + kod waluty ISO). */
    public record ExternalItem(String featureCode, long priceMinorUnits, String currencyIso) {
    }

    /** Ograniczenie kombinacji w formacie dostawcy (kind = "INCOMPATIBLE_WITH" | "DEPENDS_ON"). */
    public record ExternalRestriction(String fromFeature, String toFeature, String kind) {
    }
}
