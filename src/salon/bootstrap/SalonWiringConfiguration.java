package salon.bootstrap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import salon.billing.application.port.out.SalesIntegration;
import salon.billing.application.port.out.DocumentDatabaseRepository;
import salon.billing.application.port.out.NotificationGeneration;
import salon.billing.application.port.out.PdfGeneration;
import salon.billing.application.port.out.SettlementDatabaseRepository;
import salon.billing.application.service.DocumentGenerationService;
import salon.billing.application.service.PaymentProcessService;
import salon.billing.application.domain.model.document.AccountingDocumentFactory;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.service.InvoiceCalculationService;
import salon.billing.infrastructure.out.integration.SalesCrmIntegrationAdapter;
import salon.billing.infrastructure.in.messaging.SettlementEventListener;
import salon.billing.infrastructure.out.mock.NotificationAdapter;
import salon.billing.infrastructure.out.mock.PdfGeneratorMockAdapter;
import salon.billing.infrastructure.in.scheduling.PaymentReminderCronJobAdapter;

import salon.financing.application.port.out.BankIntegrationAcl;
import salon.financing.application.port.out.FinancingApplicationDatabaseRepository;
import salon.financing.application.service.ProcessFinancingService;
import salon.financing.application.domain.model.financing.FinancingApplicationFactory;
import salon.financing.infrastructure.out.mock.BankIntegrationMockAdapter;
import salon.financing.infrastructure.out.mock.InMemoryFinancingRepository;
import salon.financing.infrastructure.in.messaging.FinancingEventListener;

// UWAGA: Kontekst Katalogu i Konfiguratora NIE jest już spinany w tym pliku.
// Po przebudowie jest samodzielnie wstrzykiwany przez component-scan
// (@Service/@Component/@Repository) oraz salon.catalog.infrastructure.config.DomainBeansConfiguration
// (fabryki, RuleValidationService, Clock). Ręczne beany kolidowałyby z beanami skanowanymi.

import salon.logistics.application.service.InventoryManagementService;
import salon.logistics.application.port.out.ImporterACL;
import salon.logistics.application.port.out.VehicleDatabaseRepository;
import salon.logistics.application.port.out.CatalogIntegration;
import salon.logistics.infrastructure.in.messaging.FinancingEventSubscriberAdapter;
import salon.logistics.infrastructure.out.mock.FactoryIntegrationMockAdapter;
import salon.logistics.infrastructure.out.mock.InMemoryInventoryRepository;
import salon.logistics.infrastructure.out.mock.InMemorySpecificationReadModelAdapter;

import salon.sales.api.SalesQueryFacade;
import salon.sales.application.port.out.FinancingIntegrationPort;
import salon.sales.application.port.out.SpecificationPriceReadModelPort;
import salon.sales.application.service.SalesService;
import salon.sales.infrastructure.out.integration.FinancingEventBusAdapter;
import salon.sales.infrastructure.in.messaging.LogisticsEventSubscriberAdapter;
import salon.sales.infrastructure.out.mock.InMemorySpecificationPriceReadModelAdapter;

import salon.common.application.EventPublisher;

/**
 * Korzeń kompozycji (Composition Root) dla uruchomienia produkcyjnego pod Springiem.
 *
 * Usługi i adaptery Kontekstów Sprzedaży oraz Katalogu są beanami komponentowymi
 * (@Service/@Component/@Repository) i wstrzykują się przez component-scan — NIE spinamy ich tutaj.
 * Tu spinamy konteksty pozostające czystymi POJO (Rozliczenia, Logistykę, Finansowanie)
 * oraz adaptery międzykontekstowe (ACL do CRM, subskrybenty zdarzeń).
 */
@Configuration
public class SalonWiringConfiguration {

    // --- Porty wyjściowe spoza persystencji bazodanowej (na czas startu: implementacje mock) ---

    @Bean
    public VehicleDatabaseRepository inventoryRepository() {
        return new InMemoryInventoryRepository();
    }

    /** Port wyjściowy „ImporterACL" (Rysunek 37) — integracja z systemem fabryki/importera. */
    @Bean
    public ImporterACL importerAcl() {
        return new FactoryIntegrationMockAdapter();
    }

    /**
     * Port wyjściowy „CatalogIntegration" (Rysunek 37) — lokalna kopia danych Katalogu
     * zasilana asynchronicznie zdarzeniami SpecificationCompleted (Katalog) i OrderPlaced (Sprzedaż),
     * zamiast synchronicznego odpytywania innych kontekstów.
     */
    @Bean
    public CatalogIntegration catalogIntegration() {
        return new InMemorySpecificationReadModelAdapter();
    }

    /**
     * Lokalny read model wyceny specyfikacji Sprzedaży — zasilany asynchronicznie zdarzeniem
     * SpecificationCompleted (Katalog), zamiast synchronicznego odpytywania Katalogu o cenę (UC-CRM-02).
     */
    @Bean
    public SpecificationPriceReadModelPort specificationPriceReadModelPort() {
        return new InMemorySpecificationPriceReadModelAdapter();
    }

    @Bean
    public NotificationGeneration notificationPort() {
        return new NotificationAdapter();
    }

    @Bean
    public PdfGeneration pdfGeneratorPort() {
        return new PdfGeneratorMockAdapter();
    }

    /**
     * UC-FIR-01/02: dane nabywcy dociągane z Kontekstu Sprzedaży (ACL, Query po OrderId).
     * Adapter zależy wyłącznie od publicznej fasady Sprzedaży (Published Language),
     * a nie od jej repozytoriów i agregatów.
     */
    @Bean
    public SalesIntegration crmIntegrationPort(SalesQueryFacade salesQueryFacade) {
        return new SalesCrmIntegrationAdapter(salesQueryFacade);
    }

    @Bean
    public SettlementFactory settlementFactory() {
        return new SettlementFactory();
    }

    @Bean
    public BankIntegrationAcl bankIntegrationAclPort() {
        return new BankIntegrationMockAdapter();
    }

    @Bean
    public FinancingApplicationDatabaseRepository financingRepository() {
        return new InMemoryFinancingRepository();
    }

    /** Zapytanie o zdolność: adapter publikuje FinancingRequestedEvent (UC-CRM-03 -> UC-FIN-01). */
    @Bean
    public FinancingIntegrationPort financingIntegrationPort(EventPublisher eventPublisherPort) {
        return new FinancingEventBusAdapter(eventPublisherPort);
    }

    // --- Inwentarz i Logistyka: scentralizowana usługa aplikacyjna (UC-INW-01..06) ---

    @Bean
    public InventoryManagementService inventoryManagementAppService(
            VehicleDatabaseRepository inventoryRepository,
            CatalogIntegration catalogIntegration,
            ImporterACL importerAcl,
            EventPublisher eventPublisherPort) {
        return new InventoryManagementService(inventoryRepository,
                catalogIntegration, importerAcl, eventPublisherPort);
    }

    // --- Fakturowanie i Rozliczenia ---

    @Bean
    public PaymentProcessService settlementAppService(SettlementDatabaseRepository settlementRepository,
                                                      SettlementFactory settlementFactory,
                                                      DocumentDatabaseRepository documentRepository,
                                                      NotificationGeneration notificationPort,
                                                      EventPublisher eventPublisherPort) {
        return new PaymentProcessService(settlementRepository, settlementFactory,
                documentRepository, notificationPort, eventPublisherPort);
    }

    @Bean
    public DocumentGenerationService documentAppService(SettlementDatabaseRepository settlementRepository,
                                                        DocumentDatabaseRepository documentRepository,
                                                        PdfGeneration pdfGeneratorPort,
                                                        NotificationGeneration notificationPort,
                                                        EventPublisher eventPublisherPort,
                                                        SalesIntegration crmIntegrationPort) {
        return new DocumentGenerationService(settlementRepository, documentRepository,
                new InvoiceCalculationService(), new AccountingDocumentFactory(),
                pdfGeneratorPort, notificationPort, eventPublisherPort, crmIntegrationPort,
                new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"));
    }

    @Bean
    public PaymentReminderCronJobAdapter paymentReminderCronJobAdapter(
            PaymentProcessService settlementAppService) {
        return new PaymentReminderCronJobAdapter(settlementAppService);
    }

    // --- Finansowanie ---

    @Bean
    public salon.financing.application.port.out.SalesIntegration financingCrmIntegrationPort(
            SalesQueryFacade salesQueryFacade) {
        return new salon.financing.infrastructure.out.integration.SalesCrmIntegrationAdapter(salesQueryFacade);
    }

    @Bean
    public ProcessFinancingService financingAppService(FinancingApplicationDatabaseRepository financingRepository,
                                                   salon.financing.application.port.out.SalesIntegration financingCrmIntegrationPort,
                                                   BankIntegrationAcl bankIntegrationAclPort,
                                                   EventPublisher eventPublisherPort) {
        return new ProcessFinancingService(financingRepository, new FinancingApplicationFactory(),
                financingCrmIntegrationPort, bankIntegrationAclPort, eventPublisherPort);
    }

    /** Finansowanie <- Sprzedaż: FinancingRequestedEvent wyzwala UC-FIN-01 (złożenie wniosku). */
    @Bean
    public FinancingEventListener financingEventListener(ProcessFinancingService financingAppService) {
        return new FinancingEventListener(financingAppService);
    }

    // --- Katalog i Konfigurator (UC-KON-01/02) ---
    // Brak ręcznego spinania: po przebudowie kontekst wstrzykuje się sam przez component-scan
    // (BuildSpecificationService/UpdateCatalogService = @Service, adaptery = @Component/@Repository)
    // oraz salon.catalog.infrastructure.config.DomainBeansConfiguration (ProductCatalogFactory,
    // VehicleSpecificationFactory, RuleValidationService, Clock).

    // --- Adaptery sterujące (subskrybenty zdarzeń) jako beany ---

    @Bean
    public salon.sales.infrastructure.in.messaging.BillingEventSubscriberAdapter
    salesBillingEventSubscriberAdapter(SalesService salesAppService) {
        return new salon.sales.infrastructure.in.messaging.BillingEventSubscriberAdapter(salesAppService);
    }

    /** Sprzedaż <- Katalog: SpecificationCompleted zasila lokalny read model wyceny (UC-CRM-02). */
    @Bean
    public salon.sales.infrastructure.in.messaging.CatalogEventSubscriberAdapter
    catalogEventSubscriberAdapter(SalesService salesAppService) {
        return new salon.sales.infrastructure.in.messaging.CatalogEventSubscriberAdapter(salesAppService);
    }

    @Bean
    public LogisticsEventSubscriberAdapter logisticsEventSubscriberAdapter(SalesService salesAppService) {
        return new LogisticsEventSubscriberAdapter(salesAppService);
    }

    /** Inwentarz <- Sprzedaż: OrderPlaced (powiązanie spec.) oraz komenda ReleaseVehicle (UC-INW-06). */
    @Bean
    public salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter
    logisticsSalesEventSubscriberAdapter(CatalogIntegration catalogIntegration,
                                         InventoryManagementService inventoryManagementAppService) {
        return new salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter(
                catalogIntegration, inventoryManagementAppService);
    }

    /** Inwentarz <- Katalog: SpecificationCompleted zasila lokalną kopię danych Katalogu. */
    @Bean
    public salon.logistics.infrastructure.in.messaging.CatalogEventSubscriberAdapter
    logisticsCatalogEventSubscriberAdapter(CatalogIntegration catalogIntegration) {
        return new salon.logistics.infrastructure.in.messaging.CatalogEventSubscriberAdapter(
                catalogIntegration);
    }

    @Bean
    public FinancingEventSubscriberAdapter financingEventSubscriberAdapter(
            InventoryManagementService inventoryManagementAppService) {
        return new FinancingEventSubscriberAdapter(inventoryManagementAppService);
    }

    @Bean
    public salon.logistics.infrastructure.in.messaging.BillingEventSubscriberAdapter
    logisticsBillingEventSubscriberAdapter(InventoryManagementService inventoryManagementAppService) {
        return new salon.logistics.infrastructure.in.messaging.BillingEventSubscriberAdapter(
                inventoryManagementAppService, inventoryManagementAppService, inventoryManagementAppService);
    }

    @Bean
    public salon.billing.infrastructure.in.messaging.BillingEventSubscriberAdapter
    billingEventSubscriberAdapter(DocumentGenerationService documentAppService) {
        return new salon.billing.infrastructure.in.messaging.BillingEventSubscriberAdapter(
                documentAppService, documentAppService, "ksiegowy@salon.pl");
    }

    @Bean
    public SettlementEventListener settlementEventListener(PaymentProcessService settlementAppService) {
        return new SettlementEventListener(settlementAppService);
    }
}
