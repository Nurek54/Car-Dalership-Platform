package salon.catalog.application.service;

import salon.catalog.application.port.in.UpdateCatalogUseCase;
import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.application.port.out.ImporterApiPort;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;

import java.util.List;
import java.util.Optional;

/**
 * Realizuje wydanie nowej wersji cennika (WF-KAT): ściągamy opcje od Importera (ACL),
 * tworzymy NOWY aktywny cennik i ARCHIWIZUJEMY poprzedni (niezmienność starych wersji).
 *
 * Publikacja nowego aktywnego cennika emituje CatalogVersionPublishedEvent — serwis
 * ściąga zdarzenia z agregatu (pull) i przekazuje je portowi publikacji.
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
        // 1. Archiwizujemy poprzedni cennik (jeśli istnieje).
        if (previousCatalogId != null && !previousCatalogId.isBlank()) {
            Optional<ProductCatalog> previous = catalogRepository.findById(new CatalogId(previousCatalogId));
            if (previous.isPresent()) {
                ProductCatalog old = previous.get();
                old.archive();
                catalogRepository.save(old);
            }
        }

        // 2. Tworzymy nowy aktywny cennik i wypełniamy opcjami od Importera.
        ProductCatalog newCatalog = ProductCatalog.createActive(modelYear);
        List<CatalogOption> options = importerApi.fetchCurrentOptions(modelYear);
        for (int i = 0; i < options.size(); i++) {
            newCatalog.addOption(options.get(i));
        }
        catalogRepository.save(newCatalog);

        // 3. Publikujemy zdarzenia domenowe (m.in. CatalogVersionPublishedEvent).
        publishEventsOf(newCatalog);
        return newCatalog.getCatalogId();
    }

    private void publishEventsOf(ProductCatalog catalog) {
        List<DomainEvent> events = catalog.pullDomainEvents();
        for (int i = 0; i < events.size(); i++) {
            eventPublisher.publish(events.get(i));
        }
    }
}
