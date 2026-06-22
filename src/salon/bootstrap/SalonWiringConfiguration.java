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

@Configuration
public class SalonWiringConfiguration {

    

    @Bean
    public salon.sales.application.domain.model.offer.OfferFactory offerFactory() {
        return new salon.sales.application.domain.model.offer.OfferFactory();
    }

    @Bean
    public salon.sales.application.domain.model.order.OrderFactory orderFactory() {
        return new salon.sales.application.domain.model.order.OrderFactory();
    }

    

    @Bean
    public VehicleDatabaseRepository inventoryRepository() {
        return new InMemoryInventoryRepository();
    }

    
    @Bean
    public ImporterACL importerAcl() {
        return new FactoryIntegrationMockAdapter();
    }

    

    @Bean
    public InventoryManagementService inventoryManagementAppService(
            VehicleDatabaseRepository inventoryRepository,
            CatalogIntegration catalogIntegration,
            ImporterACL importerAcl,
            EventPublisher eventPublisherPort) {
        return new InventoryManagementService(inventoryRepository,
                catalogIntegration, importerAcl, eventPublisherPort);
    }

    

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

    
    @Bean
    public salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter
    logisticsSalesEventSubscriberAdapter(CatalogIntegration catalogIntegration,
                                         InventoryManagementService inventoryManagementAppService) {
        return new salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter(
                catalogIntegration, inventoryManagementAppService);
    }

    
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
