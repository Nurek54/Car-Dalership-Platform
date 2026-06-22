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

// NOTE: The Catalog and Configurator Context is no longer wired in this file.
// After the refactor it is injected on its own via component-scan
// (@Service/@Component/@Repository) and salon.catalog.infrastructure.config.DomainBeansConfiguration
// (factories, RuleValidationService, Clock). Manual beans would collide with the scanned beans.

import salon.logistics.application.service.InventoryManagementService;
import salon.logistics.application.port.out.ImporterACL;
import salon.logistics.application.port.out.VehicleDatabaseRepository;
import salon.logistics.application.port.out.CatalogIntegration;
import salon.logistics.infrastructure.in.messaging.FinancingEventSubscriberAdapter;
import salon.logistics.infrastructure.out.mock.FactoryIntegrationMockAdapter;
import salon.logistics.infrastructure.out.mock.InMemoryInventoryRepository;
import salon.logistics.infrastructure.out.mock.InMemorySpecificationReadModelAdapter;

import salon.sales.api.SalesQueryFacade;
import salon.sales.application.service.SalesService;

import salon.common.application.EventPublisher;

/**
 * Composition Root for the production run under Spring.
 *
 * The services and adapters of the Catalog Context are component beans
 * (@Service/@Component/@Repository) and are injected via component-scan — we do NOT wire them here.
 * Here we wire the contexts that remain pure POJOs (Sales, Billing, Logistics, Financing)
 * and the cross-context adapters (ACL to CRM, event subscribers).
 */
@Configuration
public class SalonWiringConfiguration {

    // --- Outbound ports outside database persistence (for startup: mock implementations) ---

    @Bean
    public VehicleDatabaseRepository inventoryRepository() {
        return new InMemoryInventoryRepository();
    }

    /** Outbound port "ImporterACL" (Figure 37) — integration with the factory/importer system. */
    @Bean
    public ImporterACL importerAcl() {
        return new FactoryIntegrationMockAdapter();
    }

    /**
     * Outbound port "CatalogIntegration" (Figure 37) — a local copy of the Catalog data
     * fed asynchronously by the SpecificationCompleted (Catalog) and OrderPlaced (Sales) events,
     * instead of synchronously querying other contexts.
     */
    @Bean
    public CatalogIntegration catalogIntegration() {
        return new InMemorySpecificationReadModelAdapter();
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
     * UC-FIR-01/02: buyer data fetched from the Sales Context (ACL, query by OrderId).
     * The adapter depends only on the public Sales facade (Published Language),
     * not on its repositories and aggregates.
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

    // --- Sales and CRM (UC-CRM-01..05) — wired as pure POJOs ---

    @Bean
    public salon.sales.application.domain.model.offer.OfferFactory offerFactory() {
        return new salon.sales.application.domain.model.offer.OfferFactory();
    }

    @Bean
    public salon.sales.application.domain.model.order.OrderFactory orderFactory() {
        return new salon.sales.application.domain.model.order.OrderFactory();
    }

    @Bean
    public salon.sales.application.port.out.CustomerDatabaseRepository salesCustomerRepository() {
        return new salon.sales.infrastructure.out.persistence.InMemoryCustomerRepository();
    }

    @Bean
    public salon.sales.application.port.out.OfferDatabaseRepository salesOfferRepository() {
        return new salon.sales.infrastructure.out.persistence.InMemoryOfferRepository();
    }

    @Bean
    public salon.sales.application.port.out.OrderDatabaseRepository salesOrderRepository() {
        return new salon.sales.infrastructure.out.persistence.InMemoryOrderRepository();
    }

    @Bean
    public salon.sales.application.port.out.CatalogIntegration salesCatalogIntegration() {
        return new salon.sales.infrastructure.out.integration.CatalogIntegrationAdapter();
    }

    @Bean
    public salon.sales.application.port.out.InventoryIntegration salesInventoryIntegration() {
        return new salon.sales.infrastructure.out.integration.InventoryIntegrationAdapter();
    }

    @Bean
    public salon.sales.application.port.out.BillingIntegration salesBillingIntegration() {
        return new salon.sales.infrastructure.out.integration.BillingIntegrationAdapter();
    }

    /** Central Sales application service (Figure 22: SalesService) — realizes 5 inbound ports. */
    @Bean
    public SalesService salesAppService(
            salon.sales.application.port.out.CustomerDatabaseRepository salesCustomerRepository,
            salon.sales.application.port.out.OfferDatabaseRepository salesOfferRepository,
            salon.sales.application.port.out.OrderDatabaseRepository salesOrderRepository,
            salon.sales.application.port.out.BillingIntegration salesBillingIntegration,
            salon.sales.application.port.out.InventoryIntegration salesInventoryIntegration,
            EventPublisher eventPublisherPort,
            salon.sales.application.domain.model.offer.OfferFactory offerFactory,
            salon.sales.application.domain.model.order.OrderFactory orderFactory) {
        return new SalesService(salesCustomerRepository, salesOfferRepository, salesOrderRepository,
                salesBillingIntegration, salesInventoryIntegration, eventPublisherPort, offerFactory, orderFactory);
    }

    /** Public Sales facade (Figure 22: SalesQueryService) consumed by Billing and Financing. */
    @Bean
    public SalesQueryFacade salesQueryFacade(
            salon.sales.application.port.out.OrderDatabaseRepository salesOrderRepository,
            salon.sales.application.port.out.OfferDatabaseRepository salesOfferRepository,
            salon.sales.application.port.out.CustomerDatabaseRepository salesCustomerRepository) {
        return new salon.sales.application.service.SalesQueryService(
                salesOrderRepository, salesOfferRepository, salesCustomerRepository);
    }

    /** Configurator application service (Figure 22: ConfiguratorAppService) — UC-CRM-01. */
    @Bean
    public salon.sales.application.port.in.StartConfigurator salesConfiguratorAppService(
            salon.sales.application.port.out.CatalogIntegration salesCatalogIntegration,
            EventPublisher eventPublisherPort) {
        return new salon.sales.application.service.ConfiguratorAppService(salesCatalogIntegration, eventPublisherPort);
    }

    // --- Inventory and Logistics: centralized application service (UC-INW-01..06) ---

    @Bean
    public InventoryManagementService inventoryManagementAppService(
            VehicleDatabaseRepository inventoryRepository,
            CatalogIntegration catalogIntegration,
            ImporterACL importerAcl,
            EventPublisher eventPublisherPort) {
        return new InventoryManagementService(inventoryRepository,
                catalogIntegration, importerAcl, eventPublisherPort);
    }

    // --- Billing and Settlement ---

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

    // --- Financing ---

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

    /** Financing <- Sales: FinancingRequestedEvent triggers UC-FIN-01 (application submission). */
    @Bean
    public FinancingEventListener financingEventListener(ProcessFinancingService financingAppService) {
        return new FinancingEventListener(financingAppService);
    }

    // --- Catalog and Configurator (UC-KON-01/02) ---
    // No manual wiring: after the refactor the context injects itself via component-scan
    // (BuildSpecificationService/UpdateCatalogService = @Service, adaptery = @Component/@Repository)
    // and salon.catalog.infrastructure.config.DomainBeansConfiguration (ProductCatalogFactory,
    // VehicleSpecificationFactory, RuleValidationService, Clock).

    // --- Driving adapters (event subscribers) as beans ---

    /** Sales <- Billing: AdvancePaymentRegistered activates the order (UC-CRM-03, part 2). */
    @Bean
    public salon.sales.infrastructure.in.messaging.BillingEventSubscriberAdapter
    salesBillingEventSubscriberAdapter(SalesService salesAppService) {
        return new salon.sales.infrastructure.in.messaging.BillingEventSubscriberAdapter(salesAppService);
    }

    /** Sales <- Catalog: SpecificationCompleted generates a proforma offer (UC-CRM-02). */
    @Bean
    public salon.sales.infrastructure.in.messaging.CatalogEventSubscriberAdapter
    salesCatalogEventSubscriberAdapter(SalesService salesAppService) {
        return new salon.sales.infrastructure.in.messaging.CatalogEventSubscriberAdapter(salesAppService);
    }

    /** Sales <- Inventory: VehicleReadyForHandover / VehicleReleaseFailed (UC-CRM-04 / 05). */
    @Bean
    public salon.sales.infrastructure.in.messaging.InventoryEventSubscriberAdapter
    salesInventoryEventSubscriberAdapter(SalesService salesAppService) {
        return new salon.sales.infrastructure.in.messaging.InventoryEventSubscriberAdapter(salesAppService);
    }

    /** Sales CronJob: rejects published offers past their validity date. */
    @Bean
    public salon.sales.infrastructure.in.cron.OfferExpirationCronJob offerExpirationCronJob(
            SalesService salesAppService) {
        return new salon.sales.infrastructure.in.cron.OfferExpirationCronJob(salesAppService);
    }

    /** Inventory <- Sales: OrderPlaced (spec. linkage) and the ReleaseVehicle command (UC-INW-06). */
    @Bean
    public salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter
    logisticsSalesEventSubscriberAdapter(CatalogIntegration catalogIntegration,
                                         InventoryManagementService inventoryManagementAppService) {
        return new salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter(
                catalogIntegration, inventoryManagementAppService);
    }

    /** Inventory <- Catalog: SpecificationCompleted feeds the local copy of the Catalog data. */
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
                documentAppService, documentAppService, "accountant@salon.pl");
    }

    @Bean
    public SettlementEventListener settlementEventListener(PaymentProcessService settlementAppService) {
        return new SettlementEventListener(settlementAppService);
    }
}
