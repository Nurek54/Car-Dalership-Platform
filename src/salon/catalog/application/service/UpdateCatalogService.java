package salon.catalog.application.service;

import salon.catalog.application.port.in.UpdateCatalog;
import salon.catalog.application.port.out.CatalogDatabaseRepository;
import salon.catalog.application.port.out.ImporterACL;
import salon.catalog.application.domain.event.CatalogUpdateFailedEvent;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Realizuje automatyczną aktualizację cennika i katalogu (UC-KON-02): ściągamy opcje od Importera
 * (ACL) i publikujemy NOWĄ aktywną wersję cennika.
 *
 * Granice transakcji (złota zasada DDD — jeden Agregat na transakcję): TA usługa zapisuje wyłącznie
 * NOWY agregat. Archiwizacja POPRZEDNIEJ wersji NIE odbywa się tutaj — jest wyzwalana zdarzeniem
 * domenowym CatalogVersionPublished i wykonywana przez ArchiveCatalogService w OSOBNEJ transakcji
 * (eventual consistency). Eliminuje to ryzyko niespójności "stary zarchiwizowany, nowy niezapisany".
 *
 * Budowę spójnego agregatu (z kompletem opcji i walidacją niezmienników pakietu) deleguje się do
 * ProductCatalogFactory — usługa aplikacyjna nie wstrzykuje opcji proceduralną pętlą.
 *
 * Sukces zapisu wieńczy zdarzenie CatalogUpdated (oraz wewnętrzne CatalogVersionPublished). Błąd
 * translacji/walidacji pakietu (scenariusz A1) przerywa aktualizację i emituje CatalogUpdateFailed.
 */
public class UpdateCatalogService implements UpdateCatalog {

    private final CatalogDatabaseRepository catalogRepository;
    private final ImporterACL importerApi;
    private final ProductCatalogFactory catalogFactory;
    private final EventPublisher eventPublisher;

    public UpdateCatalogService(CatalogDatabaseRepository catalogRepository,
                             ImporterACL importerApi,
                             ProductCatalogFactory catalogFactory,
                             EventPublisher eventPublisher) {
        if (catalogRepository == null) {
            throw new IllegalArgumentException("catalogRepository must not be null.");
        }
        if (importerApi == null) {
            throw new IllegalArgumentException("importerApi must not be null.");
        }
        if (catalogFactory == null) {
            throw new IllegalArgumentException("catalogFactory must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.catalogRepository = catalogRepository;
        this.importerApi = importerApi;
        this.catalogFactory = catalogFactory;
        this.eventPublisher = eventPublisher;
    }

    /**
     * // @Transactional w projekcie ze Springiem — zapis nowego agregatu i ściągnięcie jego zdarzeń
     * // w jednej transakcji. Publikacja zdarzeń następuje po zatwierdzeniu (AFTER_COMMIT),
     * // a archiwizacja poprzedniej wersji to już osobna transakcja po stronie handlera.
     */
    @Override
    public CatalogId publishNewCatalogVersion(String modelYear, String previousCatalogId) {
        // 1. ACL: pobranie i translacja pakietu od Importera (UC-KON-02, kroki 2-3).
        //    Walidacja LOGICZNA pakietu (niezmiennik "cennik niepusty") jest już w domenie (fabryka).
        List<CatalogOption> options;
        ProductCatalog newCatalog;
        try {
            options = importerApi.fetchCurrentOptions(modelYear);
            CatalogId previous = (previousCatalogId != null && !previousCatalogId.isBlank())
                    ? new CatalogId(previousCatalogId)
                    : null;
            // 2. Budowa spójnego agregatu przez fabrykę (komplet opcji + niezmienniki w domenie).
            newCatalog = catalogFactory.createNewVersion(modelYear, options, previous);
        } catch (RuntimeException ex) {
            // A1: błąd translacji/walidacji -> odrzucamy pakiet i emitujemy zdarzenie techniczne.
            eventPublisher.publish(new CatalogUpdateFailedEvent(
                    UUID.randomUUID(), modelYear, ex.getMessage(), Instant.now()));
            throw new IllegalStateException(
                    "Catalog update failed for modelYear " + modelYear + ": " + ex.getMessage(), ex);
        }

        // 3. Jedna transakcja = jeden agregat: zapisujemy WYŁĄCZNIE nowy aktywny cennik.
        catalogRepository.save(newCatalog);

        // 4. Propagacja: CatalogUpdated (w świat) + CatalogVersionPublished (wyzwala archiwizację
        //    poprzedniej wersji w osobnej transakcji — eventual consistency).
        publishEventsOf(newCatalog);
        return newCatalog.getCatalogId();
    }

    private void publishEventsOf(ProductCatalog catalog) {
        eventPublisher.publishAll(catalog.pullDomainEvents());
    }
}
