package salon.catalog.application.port.in;

import salon.catalog.application.domain.model.catalog.CatalogId;

/**
 * Port wejściowy dla automatycznej aktualizacji cennika i katalogu (UC-KON-02):
 * tworzymy nowy aktywny cennik i archiwizujemy poprzedni.
 */
public interface UpdateCatalog {
    CatalogId publishNewCatalogVersion(String modelYear, String previousCatalogId);
}
