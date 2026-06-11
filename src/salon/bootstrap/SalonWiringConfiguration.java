package salon.bootstrap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import salon.billing.application.port.out.CrmIntegrationPort;
import salon.billing.application.port.out.DocumentRepository;
import salon.billing.application.port.out.NotificationPort;
import salon.billing.application.port.out.PdfGeneratorPort;
import salon.billing.application.port.out.SettlementRepository;
import salon.billing.application.service.DocumentAppService;
import salon.billing.application.service.SettlementAppService;
import salon.billing.domain.model.document.AccountingDocumentFactory;
import salon.billing.domain.model.document.SellerDetails;
import salon.billing.domain.model.settlement.SettlementFactory;
import salon.billing.domain.service.InvoiceCalculationDomainService;
import salon.billing.infrastructure.integration.SalesCrmIntegrationAdapter;
import salon.billing.infrastructure.messaging.SettlementEventListener;
import salon.billing.infrastructure.mock.NotificationAdapter;
import salon.billing.infrastructure.mock.PdfGeneratorMockAdapter;
import salon.billing.infrastructure.scheduling.PaymentReminderCronJobAdapter;

import salon.financing.application.port.out.BankIntegrationAclPort;
import salon.financing.application.port.out.FinancingRepository;
import salon.financing.application.service.FinancingAppService;
import salon.financing.domain.model.financing.FinancingApplicationFactory;
import salon.financing.infrastructure.mock.BankIntegrationMockAdapter;
import salon.financing.infrastructure.mock.InMemoryFinancingRepository;

import salon.logistics.application.InventoryManagementAppService;
import salon.logistics.application.port.out.FactoryIntegrationAclPort;
import salon.logistics.application.port.out.InventoryRepository;
import salon.logistics.application.port.out.SpecificationIntegrationPort;
import salon.logistics.infrastructure.messaging.FinancingEventSubscriberAdapter;
import salon.logistics.infrastructure.mock.FactoryIntegrationMockAdapter;
import salon.logistics.infrastructure.mock.InMemoryInventoryRepository;
import salon.logistics.infrastructure.mock.SpecificationIntegrationMockAdapter;

import salon.sales.api.SalesQueryFacade;
import salon.sales.application.port.out.FinancingIntegrationPort;
import salon.sales.application.service.SalesAppService;
import salon.sales.infrastructure.integration.FinancingEventBusAdapter;
import salon.sales.infrastructure.messaging.LogisticsEventSubscriberAdapter;

import salon.shared.application.EventPublisherPort;

/**
 * Korzeń kompozycji (Composition Root) dla uruchomienia produkcyjnego pod Springiem.
 *
 * Usługi i adaptery Kontekstu Sprzedaży są beanami komponentowymi (@Service/@Component) —
 * tu spinamy pozostałe konteksty (Rozliczenia, Logistykę, Finansowanie), które pozostają
 * czystymi POJO, oraz adaptery międzykontekstowe (ACL do CRM, subskrybenty zdarzeń).
 *
 * Konfiguracja jest aktywna globalnie: pełne konteksty Springa (aplikacja, testy
 * akceptacyjne) dostają komplet usług; testy plasterkowe (@WebMvcTest/@DataJpaTest)
 * i unity nie ładują tych beanów dzięki filtrom plasterka.
 */
@Configuration
public class SalonWiringConfiguration {

    // --- Porty wyjściowe spoza persystencji bazodanowej (na czas startu: implementacje mock) ---

    @Bean
    public InventoryRepository inventoryRepository() {
        return new InMemoryInventoryRepository();
    }

    @Bean
    public FactoryIntegrationAclPort factoryIntegrationAclPort() {
        return new FactoryIntegrationMockAdapter();
    }

    @Bean
    public SpecificationIntegrationPort specificationIntegrationPort() {
        return new SpecificationIntegrationMockAdapter();
    }

    @Bean
    public NotificationPort notificationPort() {
        return new NotificationAdapter();
    }

    @Bean
    public PdfGeneratorPort pdfGeneratorPort() {
        return new PdfGeneratorMockAdapter();
    }

    /**
     * UC-FIR-01/02: dane nabywcy dociągane z Kontekstu Sprzedaży (ACL, Query po OrderId).
     * Adapter zależy wyłącznie od publicznej fasady Sprzedaży (Published Language),
     * a nie od jej repozytoriów i agregatów.
     */
    @Bean
    public CrmIntegrationPort crmIntegrationPort(SalesQueryFacade salesQueryFacade) {
        return new SalesCrmIntegrationAdapter(salesQueryFacade);
    }

    @Bean
    public SettlementFactory settlementFactory() {
        return new SettlementFactory();
    }

    @Bean
    public BankIntegrationAclPort bankIntegrationAclPort() {
        return new BankIntegrationMockAdapter();
    }

    @Bean
    public FinancingRepository financingRepository() {
        return new InMemoryFinancingRepository();
    }

    /** Zapytanie o zdolność: adapter publikuje FinancingRequestedEvent (UC-CRM-03 -> UC-FIN-01). */
    @Bean
    public FinancingIntegrationPort financingIntegrationPort(EventPublisherPort eventPublisherPort) {
        return new FinancingEventBusAdapter(eventPublisherPort);
    }

    // --- Inwentarz i Logistyka: scentralizowana usługa aplikacyjna (UC-INW-01..06) ---

    @Bean
    public InventoryManagementAppService inventoryManagementAppService(
            InventoryRepository inventoryRepository,
            SpecificationIntegrationPort specificationIntegrationPort,
            FactoryIntegrationAclPort factoryIntegrationAclPort,
            EventPublisherPort eventPublisherPort) {
        return new InventoryManagementAppService(inventoryRepository,
                specificationIntegrationPort, factoryIntegrationAclPort, eventPublisherPort);
    }

    // --- Fakturowanie i Rozliczenia ---

    @Bean
    public SettlementAppService settlementAppService(SettlementRepository settlementRepository,
                                                     SettlementFactory settlementFactory,
                                                     DocumentRepository documentRepository,
                                                     NotificationPort notificationPort,
                                                     EventPublisherPort eventPublisherPort) {
        return new SettlementAppService(settlementRepository, settlementFactory,
                documentRepository, notificationPort, eventPublisherPort);
    }

    @Bean
    public DocumentAppService documentAppService(SettlementRepository settlementRepository,
                                                 DocumentRepository documentRepository,
                                                 PdfGeneratorPort pdfGeneratorPort,
                                                 NotificationPort notificationPort,
                                                 EventPublisherPort eventPublisherPort,
                                                 CrmIntegrationPort crmIntegrationPort) {
        return new DocumentAppService(settlementRepository, documentRepository,
                new InvoiceCalculationDomainService(), new AccountingDocumentFactory(),
                pdfGeneratorPort, notificationPort, eventPublisherPort, crmIntegrationPort,
                new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"));
    }

    @Bean
    public PaymentReminderCronJobAdapter paymentReminderCronJobAdapter(
            SettlementAppService settlementAppService) {
        return new PaymentReminderCronJobAdapter(settlementAppService);
    }

    // --- Finansowanie ---

    @Bean
    public FinancingAppService financingAppService(FinancingRepository financingRepository,
                                                   BankIntegrationAclPort bankIntegrationAclPort,
                                                   EventPublisherPort eventPublisherPort) {
        return new FinancingAppService(financingRepository, new FinancingApplicationFactory(),
                bankIntegrationAclPort, eventPublisherPort);
    }

    // --- Adaptery sterujące (subskrybenty zdarzeń) jako beany ---

    @Bean
    public salon.sales.infrastructure.messaging.BillingEventSubscriberAdapter
    salesBillingEventSubscriberAdapter(SalesAppService salesAppService) {
        return new salon.sales.infrastructure.messaging.BillingEventSubscriberAdapter(salesAppService);
    }

    @Bean
    public salon.sales.infrastructure.messaging.CatalogEventSubscriberAdapter
    catalogEventSubscriberAdapter() {
        return new salon.sales.infrastructure.messaging.CatalogEventSubscriberAdapter();
    }

    @Bean
    public LogisticsEventSubscriberAdapter logisticsEventSubscriberAdapter(SalesAppService salesAppService) {
        return new LogisticsEventSubscriberAdapter(salesAppService);
    }

    @Bean
    public salon.logistics.infrastructure.messaging.SalesEventSubscriberAdapter
    logisticsSalesEventSubscriberAdapter(InventoryManagementAppService inventoryManagementAppService) {
        return new salon.logistics.infrastructure.messaging.SalesEventSubscriberAdapter(
                inventoryManagementAppService);
    }

    @Bean
    public FinancingEventSubscriberAdapter financingEventSubscriberAdapter(
            InventoryManagementAppService inventoryManagementAppService) {
        return new FinancingEventSubscriberAdapter(inventoryManagementAppService);
    }

    @Bean
    public salon.logistics.infrastructure.messaging.BillingEventSubscriberAdapter
    logisticsBillingEventSubscriberAdapter(InventoryManagementAppService inventoryManagementAppService) {
        return new salon.logistics.infrastructure.messaging.BillingEventSubscriberAdapter(
                inventoryManagementAppService, inventoryManagementAppService, inventoryManagementAppService);
    }

    @Bean
    public salon.billing.infrastructure.messaging.BillingEventSubscriberAdapter
    billingEventSubscriberAdapter(DocumentAppService documentAppService) {
        return new salon.billing.infrastructure.messaging.BillingEventSubscriberAdapter(
                documentAppService, documentAppService, "ksiegowy@salon.pl");
    }

    @Bean
    public SettlementEventListener settlementEventListener(SettlementAppService settlementAppService) {
        return new SettlementEventListener(settlementAppService);
    }
}
