package salon.catalog.infrastructure.out.acl;

/**
 * Techniczny klient zewnętrznego systemu importera/producenta („ImporterService”).
 *
 * Reprezentuje granicę do systemu Blackbox (webhook / szyna zdarzeń / API). Konkretna
 * implementacja (HTTP/SOAP/kolejka) jest dostarczana przez infrastrukturę integracyjną
 * poza tym kontekstem – mapowanie kontekstów typu KONFORMISTA po stronie transportu,
 * a tłumaczenie modelu wykonuje dopiero warstwa ACL.
 */
public interface ImporterServiceClient {

    /** Pobiera najnowszy pakiet katalogowy w obcym formacie dostawcy. */
    ExternalCatalogPackage downloadLatestPackage();
}
