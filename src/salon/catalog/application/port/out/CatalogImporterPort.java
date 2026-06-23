package salon.catalog.application.port.out;

import salon.catalog.application.dto.ImportedCatalogData;

public interface CatalogImporterPort {

    
    ImportedCatalogData fetchLatestCatalog();
}
