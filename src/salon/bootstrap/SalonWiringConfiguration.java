package salon.bootstrap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import salon.billing.application.port.out.PaymentGatewayPort;
import salon.billing.application.port.out.SettlementRepository;
import salon.billing.application.service.SettlementAppService;
import salon.billing.domain.model.settlement.SettlementFactory;
import salon.billing.application.port.out.CrmIntegrationPort;
import salon.billing.infrastructure.mock.CrmIntegrationMockAdapter;
import salon.billing.infrastructure.mock.PaymentGatewayMockAdapter;

import salon.logistics.application.AllocationAppService;
import salon.logistics.application.ProductionTrackingAppService;
import salon.logistics.application.YardManagementAppService;
import salon.logistics.application.port.out.FactoryStatusAclPort;
import salon.logistics.application.port.out.ImporterIdentityAclPort;
import salon.logistics.application.port.out.ProductionSlotRepository;
import salon.logistics.application.port.out.SpecificationIntegrationPort;
import salon.logistics.application.port.out.VehicleRepository;
import salon.logistics.domain.service.VehicleAllocationDomainService;
import salon.logistics.infrastructure.mock.FactoryStatusMockAdapter;
import salon.logistics.infrastructure.mock.ImporterIdentityMockAdapter;
import salon.logistics.infrastructure.mock.InMemoryProductionSlotRepository;
import salon.logistics.infrastructure.mock.InMemoryVehicleRepository;
import salon.logistics.infrastructure.mock.SpecificationIntegrationMockAdapter;

import salon.sales.application.port.out.OfferRepository;
import salon.sales.application.port.out.OrderRepository;
import salon.sales.application.service.OfferAppService;
import salon.sales.application.service.OrderAppService;
import salon.sales.application.service.ConfiguratorAppService;

import salon.logistics.infrastructure.messaging.SalesEventSubscriberAdapter;
import salon.sales.infrastructure.messaging.BillingEventSubscriberAdapter;
import salon.sales.infrastructure.messaging.CatalogEventSubscriberAdapter;
import salon.sales.infrastructure.messaging.LogisticsEventSubscriberAdapter;

import salon.shared.application.EventPublisherPort;

/**
 * Korzeń kompozycji (Composition Root) dla uruchomienia produkcyjnego pod Springiem.
 *
 * Spina usługi aplikacyjne kontekstów (zwykłe POJO, bez adnotacji Springa) z adapterami
 * sterowanymi (repozytoria JPA jako beany @Component) oraz adapterami sterującymi
 * (subskrybenty zdarzeń, kontrolery REST). Dzięki temu kontekstów NIE trzeba zaśmiecać
 * adnotacjami frameworka — całe okablowanie żyje tu, w warstwie integracyjnej.
 *
 * Aktywny tylko w profilu "app" — testy plasterkowe (@WebMvcTest/@DataJpaTest), Mockito i unity
 * NIE ładują tych beanów, więc pozostają lekkie i niezależne. Do realnego startu wymagane jest
 * źródło danych (DataSource) dla repozytoriów JPA — patrz docs/Architecture/InfrastructureLayerArchitecture.md.
 */
@Configuration
@Profile("app")
public class SalonWiringConfiguration {

    // --- Porty wyjściowe spoza persystencji bazodanowej (na czas startu: implementacje mock) ---

    @Bean
    public EventPublisherPort eventPublisherPort() {
        // Domyślnie no-op; w środowisku z brokerem podmień na RabbitMqEventPublisherAdapter.
        return event -> { };
    }

    @Bean
    public PaymentGatewayPort paymentGatewayPort() {
        return new PaymentGatewayMockAdapter();
    }

    @Bean
    public VehicleRepository vehicleRepository() {
        return new InMemoryVehicleRepository();
    }

    @Bean
    public ProductionSlotRepository productionSlotRepository() {
        return new InMemoryProductionSlotRepository();
    }

    @Bean
    public ImporterIdentityAclPort importerIdentityAclPort() {
        return new ImporterIdentityMockAdapter();
    }

    @Bean
    public SpecificationIntegrationPort specificationIntegrationPort() {
        return new SpecificationIntegrationMockAdapter();
    }

    @Bean
    public CrmIntegrationPort crmIntegrationPort() {
        return new CrmIntegrationMockAdapter();
    }

    @Bean
    public VehicleAllocationDomainService vehicleAllocationDomainService() {
        return new VehicleAllocationDomainService();
    }

    @Bean
    public SettlementFactory settlementFactory() {
        return new SettlementFactory();
    }

    // --- Usługi aplikacyjne kontekstów (spięte z adapterami sterowanymi) ---

    @Bean
    public OfferAppService offerAppService(OfferRepository offerRepository) {
        return new OfferAppService(offerRepository);
    }

    @Bean
    public ConfiguratorAppService configuratorAppService(EventPublisherPort eventPublisherPort) {
        return new ConfiguratorAppService(eventPublisherPort);
    }

    @Bean
    public OrderAppService orderAppService(OrderRepository orderRepository,
                                           OfferRepository offerRepository,
                                           EventPublisherPort eventPublisherPort) {
        return new OrderAppService(orderRepository, offerRepository, eventPublisherPort);
    }

    @Bean
    public SettlementAppService settlementAppService(SettlementRepository settlementRepository,
                                                     SettlementFactory settlementFactory,
                                                     EventPublisherPort eventPublisherPort,
                                                     PaymentGatewayPort paymentGatewayPort) {
        return new SettlementAppService(settlementRepository, settlementFactory,
                eventPublisherPort, paymentGatewayPort);
    }

    @Bean
    public FactoryStatusAclPort factoryStatusAclPort() {
        return new FactoryStatusMockAdapter();
    }

    @Bean
    public YardManagementAppService yardManagementAppService(VehicleRepository vehicleRepository,
                                                             ImporterIdentityAclPort importerIdentityAclPort,
                                                             EventPublisherPort eventPublisherPort) {
        return new YardManagementAppService(vehicleRepository, importerIdentityAclPort, eventPublisherPort);
    }

    @Bean
    public ProductionTrackingAppService productionTrackingAppService(ProductionSlotRepository productionSlotRepository,
                                                                     FactoryStatusAclPort factoryStatusAclPort,
                                                                     EventPublisherPort eventPublisherPort) {
        return new ProductionTrackingAppService(productionSlotRepository, factoryStatusAclPort, eventPublisherPort);
    }

    @Bean
    public AllocationAppService allocationAppService(VehicleRepository vehicleRepository,
                                                     ProductionSlotRepository productionSlotRepository,
                                                     SpecificationIntegrationPort specificationIntegrationPort,
                                                     VehicleAllocationDomainService vehicleAllocationDomainService,
                                                     EventPublisherPort eventPublisherPort) {
        return new AllocationAppService(vehicleRepository, productionSlotRepository,
                specificationIntegrationPort, vehicleAllocationDomainService, eventPublisherPort);
    }

    // --- Adaptery sterujące (subskrybenty zdarzeń) jako beany ---

    @Bean
    public BillingEventSubscriberAdapter billingEventSubscriberAdapter(OrderAppService orderAppService) {
        return new BillingEventSubscriberAdapter(orderAppService);
    }

    @Bean
    public CatalogEventSubscriberAdapter catalogEventSubscriberAdapter(OfferAppService offerAppService) {
        return new CatalogEventSubscriberAdapter(offerAppService);
    }

    @Bean
    public LogisticsEventSubscriberAdapter logisticsEventSubscriberAdapter(OrderAppService orderAppService) {
        return new LogisticsEventSubscriberAdapter(orderAppService);
    }

    @Bean
    public SalesEventSubscriberAdapter salesEventSubscriberAdapter(AllocationAppService allocationAppService) {
        return new SalesEventSubscriberAdapter(allocationAppService);
    }
}
