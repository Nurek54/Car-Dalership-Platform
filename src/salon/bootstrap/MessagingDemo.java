package salon.bootstrap;

import salon.billing.application.command.GenerateAdvanceCommand;
import salon.billing.application.command.ProcessPaymentCommand;
import salon.billing.application.service.DocumentAppService;
import salon.billing.application.service.SettlementAppService;
import salon.billing.domain.model.document.AccountingDocumentFactory;
import salon.billing.domain.model.document.SellerDetails;
import salon.billing.domain.model.settlement.SettlementFactory;
import salon.billing.domain.service.InvoiceCalculationDomainService;
import salon.billing.infrastructure.integration.SalesCrmIntegrationAdapter;
import salon.billing.infrastructure.messaging.OrderReadyForSettlementEvent;
import salon.billing.infrastructure.messaging.SettlementEventListener;
import salon.billing.infrastructure.mock.InMemoryDocumentRepository;
import salon.billing.infrastructure.mock.InMemorySettlementRepository;
import salon.billing.infrastructure.mock.NotificationMockAdapter;
import salon.billing.infrastructure.mock.PdfGeneratorMockAdapter;
import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.sales.application.service.SalesAppService;
import salon.sales.application.service.SalesQueryService;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.domain.model.customer.Address;
import salon.sales.domain.model.customer.ContactData;
import salon.sales.domain.model.customer.Customer;
import salon.sales.domain.model.customer.CustomerId;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.infrastructure.messaging.SalesDepositListener;
import salon.sales.infrastructure.mock.InMemoryCustomerRepository;
import salon.sales.infrastructure.mock.InMemoryOfferRepository;
import salon.sales.infrastructure.mock.InMemoryOrderRepository;
import salon.shared.application.EventPublisherPort;
import salon.shared.infrastructure.messaging.RabbitMqConfig;
import salon.shared.infrastructure.messaging.RabbitMqConnection;
import salon.shared.infrastructure.messaging.RabbitMqEventConsumer;
import salon.shared.infrastructure.messaging.RabbitMqEventPublisherAdapter;
import salon.shared.infrastructure.messaging.RecordEventSerializer;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Demo (RabbitMQ): asynchroniczna choreografia UC-CRM-03 cz.2 (Rys. 19/20 PDF) —
 * Rozliczenia księgują zadatek i publikują PaymentRegisteredEvent na brokerze,
 * a SalesDepositListener (idempotentny po eventId) aktywuje zamówienie w CRM.
 *
 * Wymaga uruchomionego brokera (docker-compose up rabbitmq).
 */
public class MessagingDemo {

    public static void main(String[] args) throws Exception {
        try (RabbitMqConnection connection = new RabbitMqConnection()) {
            RecordEventSerializer serializer = new RecordEventSerializer();

            // --- SPRZEDAŻ I CRM: oferta -> zamówienie (DRAFT) + konsument zadatku ---
            InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
            InMemoryOfferRepository offerRepo = new InMemoryOfferRepository();
            InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
            EventPublisherPort salesPublisher = new RabbitMqEventPublisherAdapter(connection, serializer);
            SalesAppService sales = new SalesAppService(
                    customerRepo, offerRepo, orderRepo, salesPublisher,
                    new FixedPriceCatalog(), null, null);

            sales.registerCustomer(new Customer(new CustomerId("CUST-DEMO"), "Jan Kowalski",
                    "1234563218", new Address("Marszałkowska 1", "00-001", "Warszawa", "PL"),
                    new ContactData("jan.kowalski@example.com", "+48 600 100 200")));
            sales.startConfiguratorSession(new StartConfiguratorSessionCommand("CUST-DEMO", "SP-7"));
            OfferId offerId = sales.generateOffer("CUST-DEMO", "SPEC-DEMO");
            String orderId = sales.acceptOfferAndCreateOrder(offerId);

            RabbitMqEventConsumer salesConsumer =
                    new RabbitMqEventConsumer(connection, RabbitMqConfig.SALES_QUEUE);
            salesConsumer.register("PaymentRegisteredEvent", new SalesDepositListener(sales));
            salesConsumer.start();

            // --- FAKTUROWANIE I ROZLICZENIA ---
            EventPublisherPort billingPublisher = new RabbitMqEventPublisherAdapter(connection, serializer);
            InMemorySettlementRepository settlementRepo = new InMemorySettlementRepository();
            InMemoryDocumentRepository documentRepo = new InMemoryDocumentRepository();

            SettlementAppService settlements = new SettlementAppService(
                    settlementRepo, new SettlementFactory(), documentRepo,
                    new NotificationMockAdapter(), billingPublisher);

            SettlementEventListener listener = new SettlementEventListener(settlements);
            listener.on(new OrderReadyForSettlementEvent(
                    UUID.randomUUID(), orderId, new BigDecimal("100000"), "PLN", Instant.now()));

            DocumentAppService docs = new DocumentAppService(
                    settlementRepo, documentRepo,
                    new InvoiceCalculationDomainService(), new AccountingDocumentFactory(),
                    new PdfGeneratorMockAdapter(), new NotificationMockAdapter(), billingPublisher,
                    new SalesCrmIntegrationAdapter(new SalesQueryService(orderRepo, offerRepo, customerRepo)),
                    new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"));

            System.out.println(">> generateAdvance(" + orderId + ") -> AdvancePaymentRequestedEvent (UC-FIR-01)...");
            docs.generateAdvance(new GenerateAdvanceCommand(orderId, "ksiegowy@salon.pl"));

            System.out.println(">> processPayment (zadatek) -> PaymentRegisteredEvent na brokerze...");
            settlements.processPayment(new ProcessPaymentCommand(
                    orderId, "TX-DEMO-1", new BigDecimal("10000"), "PLN"));

            // Dajemy konsumentowi chwilę na odebranie wiadomości z kolejki.
            Thread.sleep(1500);
            System.out.println(">> Stan zamówienia po przejściu zdarzenia przez kolejkę: "
                    + orderRepo.findById(new OrderId(orderId)).get().getState());
        }
    }

    /** Cennik demo: stała wycena specyfikacji (UC-CRM-02). */
    private static final class FixedPriceCatalog implements CatalogRepository {
        @Override
        public Money getSpecificationPrice(String specificationId) {
            return Money.of(new BigDecimal("100000"), "PLN");
        }

        @Override
        public void save(ProductCatalog catalog) {
            throw new UnsupportedOperationException("Demo price list is read-only.");
        }

        @Override
        public Optional<ProductCatalog> findById(CatalogId id) {
            return Optional.empty();
        }

        @Override
        public List<ProductCatalog> findAll() {
            return List.of();
        }
    }
}
