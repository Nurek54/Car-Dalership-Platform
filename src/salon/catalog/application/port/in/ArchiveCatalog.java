package salon.catalog.application.port.in;

import salon.catalog.application.domain.model.catalog.CatalogId;

/**
 * Port wejściowy: archiwizacja poprzedniej wersji cennika (UC-KON-02).
 *
 * Wydzielony jako osobny use case, bo archiwizacja STAREGO agregatu odbywa się w OSOBNEJ
 * transakcji niż publikacja NOWEGO (eventual consistency) — wyzwalana zdarzeniem
 * CatalogVersionPublished. Dzięki temu jedna transakcja modyfikuje tylko jeden Agregat.
 */
public interface ArchiveCatalog {
    void archivePreviousVersion(CatalogId catalogId);
}
