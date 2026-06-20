package salon.catalog.application.port.out;

import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;

import java.util.Optional;

/**
 * PORT WYJŚCIOWY – repozytorium agregatu ProductCatalog („CatalogDatabaseRepository”).
 *
 * Utrwala i globalnie udostępnia agregaty jednego typu; tworzy złudzenie
 * przechowywania wszystkich katalogów w pamięci i uniezależnia aplikację od
 * technologii magazynu. Repozytorium NIE kontroluje transakcji i NIE tworzy agregatów.
 *
 * Definicja portu (abstrakcja) należy do warstwy aplikacji; implementuje ją adapter
 * w warstwie infrastruktury (reguła odwrócenia zależności – D z SOLID).
 */
public interface ProductCatalogRepository {

    void save(ProductCatalog catalog);

    Optional<ProductCatalog> findById(CatalogId id);

    /** Aktywny katalog dla danego rocznika modelowego (źródło cen w UC-KON-01). */
    Optional<ProductCatalog> findActiveByModelYear(ModelYear modelYear);
}
