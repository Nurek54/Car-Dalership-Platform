package salon.catalog.infrastructure.out.acl;

import salon.catalog.application.port.out.CatalogImporterPort;
import salon.catalog.application.dto.ImportedCatalogData;
import org.springframework.stereotype.Component;

/**
 * ADAPTER WYJŚCIOWY (ACL) – implementacja portu {@link CatalogImporterPort}
 * („ImporterACL” → ImporterService z diagramu).
 *
 * Łączy techniczny klient zewnętrzny ({@link ImporterServiceClient}) z translatorem
 * modeli ({@link ImporterCatalogTranslator}). Dzięki temu kontekst Katalogu otrzymuje
 * wyłącznie dane w modelu lokalnym i pozostaje odporny na zmiany formatu dostawcy
 * (warstwa zapobiegająca uszkodzeniu).
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
