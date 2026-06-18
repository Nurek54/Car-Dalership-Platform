package salon.catalog.infrastructure.in.messaging;

import salon.catalog.application.domain.event.CatalogVersionPublishedEvent;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.port.in.ArchiveCatalog;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter sterujący (driving) — subskrybent zdarzenia CatalogVersionPublished WEWNĄTRZ
 * Kontekstu Katalogu (UC-KON-02).
 *
 * Realizuje regułę DDD "jeden agregat na transakcję": publikacja nowej wersji cennika zapisuje
 * tylko NOWY agregat, a archiwizacja POPRZEDNIEJ wersji dzieje się tutaj — w osobnej transakcji
 * (eventual consistency). Jeśli zdarzenie nie niesie previousCatalogId (pierwsza wersja rocznika),
 * nie ma czego archiwizować.
 *
 * Idempotencja: duplikaty (ten sam eventId) są ignorowane po stronie subskrybenta.
 */
public class CatalogVersionPublishedEventListener {

    private final ArchiveCatalog archiveCatalog;
    private final Set<UUID> processedEventIds = ConcurrentHashMap.newKeySet();

    public CatalogVersionPublishedEventListener(ArchiveCatalog archiveCatalog) {
        if (archiveCatalog == null) {
            throw new IllegalArgumentException("archiveCatalog must not be null.");
        }
        this.archiveCatalog = archiveCatalog;
    }

    /** Opublikowano nową wersję cennika -> archiwizacja poprzedniej w osobnej transakcji. */
    public void on(CatalogVersionPublishedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (isDuplicate(event.eventId())) {
            return;
        }
        String previousCatalogId = event.previousCatalogId();
        if (previousCatalogId == null || previousCatalogId.isBlank()) {
            return; // pierwsza wersja rocznika — brak poprzednika do archiwizacji
        }
        this.archiveCatalog.archivePreviousVersion(new CatalogId(previousCatalogId));
        System.out.println("[CatalogVersionPublishedEventListener] Zarchiwizowano poprzedni cennik "
                + previousCatalogId + " po publikacji wersji " + event.newCatalogId() + ".");
    }

    private boolean isDuplicate(UUID eventId) {
        boolean firstTime = this.processedEventIds.add(eventId);
        if (!firstTime) {
            System.out.println("[CatalogVersionPublishedEventListener] Duplicate event ignored: " + eventId);
        }
        return !firstTime;
    }
}
