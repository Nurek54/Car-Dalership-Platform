package salon.catalog.application.port.out;

import salon.catalog.domain.model.catalog.CatalogOption;

import java.util.List;

/**
 * Port wyjściowy (ACL): pobranie aktualnej oferty/cennika od Importera (system zewnętrzny).
 * Wykorzystywany przy automatycznej aktualizacji cennika (UC-KON-02).
 */
public interface ImporterApiPort {
    List<CatalogOption> fetchCurrentOptions(String modelYear);
}
