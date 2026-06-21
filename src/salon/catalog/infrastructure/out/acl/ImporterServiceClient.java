package salon.catalog.infrastructure.out.acl;

/**
 * Technical client of the external importer/manufacturer system ("ImporterService").
 *
 * Represents the boundary to the Blackbox system (webhook / event bus / API). The concrete
 * implementation (HTTP/SOAP/queue) is provided by the integration infrastructure
 * outside this context – a CONFORMIST context mapping on the transport side,
 * while the model translation is performed only by the ACL layer.
 */
public interface ImporterServiceClient {

    /** Fetches the latest catalog package in the vendor's foreign format. */
    ExternalCatalogPackage downloadLatestPackage();
}
