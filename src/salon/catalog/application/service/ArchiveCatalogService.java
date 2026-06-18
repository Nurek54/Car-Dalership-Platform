package salon.catalog.application.service;

import salon.catalog.application.port.in.ArchiveCatalog;
import salon.catalog.application.port.out.CatalogDatabaseRepository;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.ProductCatalog;

import java.util.Optional;

/**
 * Usługa aplikacyjna: archiwizacja POPRZEDNIEJ wersji cennika (UC-KON-02).
 *
 * Wywoływana asynchronicznie po opublikowaniu nowej wersji (zdarzenie CatalogVersionPublished),
 * w OSOBNEJ transakcji niż zapis nowego cennika. Modyfikuje DOKŁADNIE JEDEN agregat
 * (stary ProductCatalog) — zgodnie ze złotą zasadą DDD o granicach spójności.
 *
 * // @Transactional w projekcie ze Springiem — pobranie + archiwizacja + zapis starej wersji
 * // w jednej, niezależnej transakcji (eventual consistency wobec publikacji nowej wersji).
 */
public class ArchiveCatalogService implements ArchiveCatalog {

    private final CatalogDatabaseRepository catalogRepository;

    public ArchiveCatalogService(CatalogDatabaseRepository catalogRepository) {
        if (catalogRepository == null) {
            throw new IllegalArgumentException("catalogRepository must not be null.");
        }
        this.catalogRepository = catalogRepository;
    }

    @Override
    public void archivePreviousVersion(CatalogId catalogId) {
        if (catalogId == null) {
            throw new IllegalArgumentException("catalogId must not be null.");
        }
        Optional<ProductCatalog> previous = catalogRepository.findById(catalogId);
        if (previous.isEmpty()) {
            // Idempotencja: brak starej wersji (np. pierwsza publikacja) — nic do zrobienia.
            return;
        }
        ProductCatalog old = previous.get();
        old.archive();
        catalogRepository.save(old);
    }
}
