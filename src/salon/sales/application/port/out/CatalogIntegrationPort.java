package salon.sales.application.port.out;

/**
 * Port wyjściowy (driven) do Kontekstu Katalogu i Konfiguratora.
 * UC-CRM-01: otwarcie interfejsu konfiguratora dla zainicjowanej sesji.
 */
public interface CatalogIntegrationPort {

    void openConfiguratorInterface(String sessionId, String customerId, String salespersonId);
}
