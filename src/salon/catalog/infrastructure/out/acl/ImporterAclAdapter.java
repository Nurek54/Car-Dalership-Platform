package salon.catalog.infrastructure.out.acl;

import salon.catalog.application.port.out.CatalogImporterPort;
import salon.catalog.application.dto.ImportedCatalogData;
import org.springframework.stereotype.Component;

/**
 * OUTBOUND ADAPTER (ACL) – implementation of the {@link CatalogImporterPort} port
 * („ImporterACL” → ImporterService z diagramu).
 *
 * Combines the technical external client ({@link ImporterServiceClient}) with the model
 * translator ({@link ImporterCatalogTranslator}). Thanks to this the Catalog context receives
 * only data in the local model and stays resilient to changes in the vendor format
 * (anti-corruption layer).
 */
@Component
public class ImporterAclAdapter implements CatalogImporterPort {

    private final ImporterServiceClient client;
    private final ImporterCatalogTranslator translator;

    public ImporterAclAdapter(ImporterServiceClient client, ImporterCatalogTranslator translator) {
        this.client = client;
        this.translator = translator;
    }

    @Override
    public ImportedCatalogData fetchLatestCatalog() {
        ExternalCatalogPackage external = client.downloadLatestPackage();
        return translator.translate(external);
    }
}
