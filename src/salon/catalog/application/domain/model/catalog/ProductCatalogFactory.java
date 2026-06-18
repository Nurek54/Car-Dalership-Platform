package salon.catalog.application.domain.model.catalog;

import java.util.List;

/**
 * Fabryka agregatu ProductCatalog (UC-KON-02, inicjalizacja).
 *
 * Powołuje do życia NOWĄ aktywną wersję cennika z kompletem opcji uzyskanych od Importera (ACL),
 * trzymając logikę poprawnej konstrukcji i ochronę niezmienników z dala od usługi aplikacyjnej.
 * Agregat powstaje od razu w prawidłowym stanie — bez "pustego" cennika dopełnianego proceduralnie.
 */
public class ProductCatalogFactory {

    /**
     * @param modelYear         rocznik/wersja cennika
     * @param options           komplet opcji pakietu (nie może być pusty — niezmiennik domeny)
     * @param previousCatalogId id poprzedniej wersji do archiwizacji (null dla pierwszej wersji)
     */
    public ProductCatalog createNewVersion(String modelYear,
                                            List<CatalogOption> options,
                                            CatalogId previousCatalogId) {
        return ProductCatalog.publishNewVersion(new ModelYear(modelYear), options, previousCatalogId);
    }
}
