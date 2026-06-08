package salon.catalog.application.service;

import salon.catalog.application.port.in.UpdateCatalogUseCase;
import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.application.port.out.ImporterApiPort;
import salon.catalog.domain.event.CatalogUpdateFailedEvent;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Realizuje automatyczną aktualizację cennika i katalogu (UC-KON-02): ściągamy opcje od Importera
 * (ACL), tworzymy NOWY aktywny cennik i ARCHIWIZUJEMY poprzedni (niezmienność starych wersji).
 *
 * Sukces zapisu wieńczy zdarzenie CatalogUpdated. Błąd translacji/walidacji pakietu (scenariusz A1)
 * przerywa aktualizację i emituje techniczne zdarzenie CatalogUpdateFailed.
 */
public class CatalogAppService implements UpdateCatalogUseCase {

    private final CatalogRepository catalogRepository;
    private final ImporterApiPort importerApi;
    private final EventPublisherPort eventPublisher;

    public CatalogAppService(CatalogRepository catalogRepository,
                             ImporterApiPort importerApi,
                             EventPublisherPort eventPublisher) {
        if (catalogRepository == null) {
            throw new IllegalArgumentException("catalogRepository must not be null.");
        }
        if (importerApi == null) {
            throw new IllegalArgumentException("importerApi must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.catalogRepository = catalogRepository;
        this.importerApi = importerApi;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CatalogId publishNewCatalogVersion(String modelYear, String previousCatalogId) {
        // 1. ACL: pobranie, translacja i walidacja pakietu od Importera (UC-KON-02, kroki 2-3).
        List<CatalogOption> options;
        try {
            options = importerApi.fetchCurrentOptions(modelYear);
            validateImportedPackage(options);
        } catch (RuntimeException ex) {
            // A1: błąd translacji/walidacji -> odrzucamy pakiet i emitujemy zdarzenie techniczne.
            eventPublisher.publish(new CatalogUpdateFailedEvent(
                    UUID.randomUUID(), modelYear, ex.getMessage(), Instant.now()));
            throw new IllegalStateException(
                    "Catalog update failed for modelYear " + modelYear + ": " + ex.getMessage(), ex);
        }

        // 2. Archiwizujemy poprzedni cennik (jeśli istnieje) — niemutowalność starych wersji.
        if (previousCatalogId != null && !previousCatalogId.isBlank()) {
            Optional<ProductCatalog> previous = catalogRepository.findById(new CatalogId(previousCatalogId));
            if (previous.isPresent()) {
                ProductCatalog old = previous.get();
                old.archive();
                catalogRepository.save(old);
            }
        }

        // 3. Tworzymy nowy aktywny cennik (podbita wersja) i wypełniamy opcjami od Importera.
        ProductCatalog newCatalog = ProductCatalog.createActive(modelYear);
        for (int i = 0; i < options.size(); i++) {
            newCatalog.addOption(options.get(i));
        }
        catalogRepository.save(newCatalog);

        // 4. Propagacja zmian: emisja zdarzenia CatalogUpdated (CatalogUpdatedEvent).
        publishEventsOf(newCatalog);
        return newCatalog.getCatalogId();
    }

    // Walidacja logiczna pakietu (UC-KON-02 krok 3): pusty pakiet/brak cen traktujemy jako błąd.
    private void validateImportedPackage(List<CatalogOption> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Pakiet katalogowy jest pusty (brak opcji/cen).");
        }
    }

    private void publishEventsOf(ProductCatalog catalog) {
        List<DomainEvent> events = catalog.pullDomainEvents();
        for (int i = 0; i < events.size(); i++) {
            eventPublisher.publish(events.get(i));
        }
    }
}
