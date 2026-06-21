package salon.catalog.application.port.out;

import salon.catalog.application.dto.ImportedCatalogData;

/**
 * OUTBOUND PORT – querying the external manufacturer/importer system for the latest
 * catalog package ("ImporterACL" → ImporterService).
 *
 * Context mapping: ANTI-CORRUPTION LAYER (ACL). The adapter
 * implementing this port translates the external (foreign) format into the local model before
 * the data reaches the application layer – the Catalog context does not depend on the importer's model.
 */
public interface CatalogImporterPort {

    /**
     * Returns catalog data already TRANSLATED into the local model (domain value
     * objects). Building the aggregate from this data is the task of the factory in the application service.
     */
    ImportedCatalogData fetchLatestCatalog();
}
