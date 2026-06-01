package salon.catalog.application.port.in;

import salon.catalog.domain.model.catalog.CatalogId;

/**
 * Port wejściowy dla wydania nowej wersji cennika (WF-KAT):
 * tworzymy nowy aktywny cennik i archiwizujemy poprzedni.
 */
public interface UpdateCatalogUseCase {
    CatalogId publishNewCatalogVersion(String modelYear, String previousCatalogId);
}
