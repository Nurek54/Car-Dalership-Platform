package salon.catalog.infrastructure.out.persistence;

import java.util.List;
import java.util.Optional;

/**
 * Techniczna granica trwałego magazynu („DBAdapter” z diagramu) – cienki kontrakt
 * do faktycznego sterownika bazy danych.
 *
 * Adaptery repozytoriów (SpecificationDatabaseRepository, CatalogDatabaseRepository)
 * tłumaczą model dziedziny na rekordy trwałego magazynu i delegują do tego interfejsu.
 * Konkretna implementacja (JDBC/JPA/NoSQL) leży POZA kontekstem Katalogu – to
 * zewnętrzny szczegół infrastruktury, dlatego tutaj występuje wyłącznie jako port.
 *
 * Repozytorium typu „trwały magazyn” (jak HashMap): {@link #upsert} zapisuje/nadpisuje.
 */
public interface DbAdapter {

    void upsert(String collection, String id, Object record);

    Optional<Object> findById(String collection, String id);

    List<Object> findAll(String collection);
}
