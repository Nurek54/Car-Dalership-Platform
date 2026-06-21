package salon.catalog.application.port.in;

/**
 * INBOUND PORT (contract) – "UpdateCatalog" from the ports-and-adapters diagram.
 *
 * Exposes the service for automatic update of the price list and catalog (UC-KON-02).
 * Invoked by the CronJob inbound adapter (periodic task) or by an event
 * integracyjne z systemu producenta/importera.
 */
public interface UpdateCatalog {

    /**
     * Fetches, translates (ACL), validates and saves the new catalog, archiving the current one,
     * and then emits CatalogUpdated. On error it emits CatalogUpdateFailed.
     */
    void update();
}
