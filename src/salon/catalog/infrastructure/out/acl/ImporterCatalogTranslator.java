package salon.catalog.infrastructure.out.acl;

import salon.catalog.application.dto.ImportedCatalogData;
import salon.catalog.application.domain.exception.CatalogValidationException;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Translator warstwy zapobiegającej uszkodzeniu (ACL).
 *
 * Tłumaczy obcy model {@link ExternalCatalogPackage} na obiekty wartości modelu
 * lokalnego. Dane z kontekstu „na górze” modelujemy jako obiekty wartości (PDF, rozdz. 3).
 * Błąd niezgodności formatu zgłaszany jest jako {@link CatalogValidationException}
 * (UC-KON-02 / A1 – błąd translacji danych).
 */
@Component
public class ImporterCatalogTranslator {

    public ImportedCatalogData translate(ExternalCatalogPackage external) {
        try {
            ModelYear modelYear = ModelYear.of(external.year());

            List<CatalogOption> options = external.priceList().stream()
                    .map(this::toOption)
                    .toList();

            List<CatalogRule> rules = external.restrictions().stream()
                    .map(this::toRule)
                    .toList();

            return new ImportedCatalogData(modelYear, options, rules);
        } catch (CatalogValidationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new CatalogValidationException(
                    "Niezgodny format pakietu katalogowego: " + e.getMessage(), e);
        }
    }

    private CatalogOption toOption(ExternalCatalogPackage.ExternalItem item) {
        // Dostawca podaje cenę w jednostkach podrzędnych (grosze) – przeliczenie na kwotę główną.
        BigDecimal price = BigDecimal.valueOf(item.priceMinorUnits(), 2);
        return new CatalogOption(OptionCode.of(item.featureCode()), Money.of(price, item.currencyIso()));
    }

    private CatalogRule toRule(ExternalCatalogPackage.ExternalRestriction restriction) {
        RuleType type = switch (restriction.kind()) {
            case "INCOMPATIBLE_WITH" -> RuleType.EXCLUDES;
            case "DEPENDS_ON" -> RuleType.REQUIRES;
            default -> throw new CatalogValidationException(
                    "Nieznany typ ograniczenia dostawcy: " + restriction.kind());
        };
        return new CatalogRule(
                OptionCode.of(restriction.fromFeature()),
                OptionCode.of(restriction.toFeature()),
                type);
    }
}
