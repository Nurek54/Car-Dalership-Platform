package salon.bootstrap;

import salon.billing.application.command.GenerateAdvanceCommand;
import salon.billing.application.command.ProcessPaymentCommand;
import salon.billing.application.service.DocumentGenerationService;
import salon.billing.application.service.PaymentProcessService;
import salon.billing.application.domain.model.document.AccountingDocumentFactory;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.service.InvoiceCalculationService;
import salon.billing.infrastructure.out.integration.SalesCrmIntegrationAdapter;
import salon.billing.infrastructure.in.messaging.OrderReadyForSettlementEvent;
import salon.billing.infrastructure.in.messaging.SettlementEventListener;
import salon.billing.infrastructure.out.mock.InMemoryDocumentRepository;
import salon.billing.infrastructure.out.mock.InMemorySettlementRepository;
import salon.billing.infrastructure.out.mock.NotificationMockAdapter;
import salon.billing.infrastructure.out.mock.PdfGeneratorMockAdapter;
import salon.sales.application.command.AcceptOfferCommand;
import salon.sales.application.domain.model.customer.Address;
import salon.sales.application.domain.model.customer.ContactData;
import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.model.offer.OfferFactory;
import salon.sales.application.domain.model.order.OrderFactory;
import salon.sales.application.domain.model.order.PaymentMethod;
import salon.sales.application.service.SalesService;
import salon.sales.application.service.SalesQueryService;
import salon.sales.infrastructure.in.messaging.SalesDepositListener;
import salon.sales.infrastructure.out.integration.BillingIntegrationAdapter;
import salon.sales.infrastructure.out.integration.InventoryIntegrationAdapter;
import salon.sales.infrastructure.out.persistence.InMemoryCustomerRepository;
import salon.sales.infrastructure.out.persistence.InMemoryOfferRepository;
import salon.sales.infrastructure.out.persistence.InMemoryOrderRepository;
import salon.common.application.EventPublisher;
import salon.common.infrastructure.messaging.RabbitMqConfig;
import salon.common.infrastructure.messaging.RabbitMqConnection;
import salon.common.infrastructure.messaging.RabbitMqEventConsumer;
import salon.common.infrastructure.messaging.RabbitMqEventPublisherAdapter;
import salon.common.infrastructure.messaging.RecordEventSerializer;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Demo (RabbitMQ): asynchronous choreography of UC-CRM-03 part 2 (Figs. 19/20 of the PDF) —
 * Billing posts the deposit and publishes PaymentRegisteredEvent on the broker, and
 * SalesDepositListener (idempotent by eventId) activates the order in the CRM.
 *
 * Requires a running broker (docker-compose up rabbitmq).
 */
public class MessagingDemo {

    public static void main(String[] args) throws Exception {
        try (RabbitMqConnection connection = new RabbitMqConnection()) {
            RecordEventSerializer serializer = new RecordEventSerializer();

            // --- SALES AND CRM: offer -> order + deposit consumer ---
            InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
            InMemoryOfferRepository offerRepo = new InMemoryOfferRepository();
            InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
            EventPublisher salesPublisher = new RabbitMqEventPublisherAdapter(connection, serializer);
            SalesService sales = new SalesService(
                    customerRepo, offerRepo, orderRepo,
                    new BillingIntegrationAdapter(), new InventoryIntegrationAdapter(), salesPublisher,
                    new OfferFactory(), new OrderFactory());

            sales.registerCustomer(new Customer(new CustomerId("CUST-DEMO"), "Jan Kowalski",
                    "1234563218", new Address("Main Street 1", "00-001", "Warsaw", "PL"),
                    new ContactData("jan.kowalski@example.com", "+48 600 100 200")));
            // The catalog pricing would arrive via the SpecificationCompleted event; here we pass it directly.
            String offerId = sales.createProformaOffer("CUST-DEMO", "SPEC-DEMO", Money.of(100000, "PLN"));
            String orderId = sales.acceptOffer(new AcceptOfferCommand(offerId, PaymentMethod.BANK_TRANSFER));

            RabbitMqEventConsumer salesConsumer =
                    new RabbitMqEventConsumer(connection, RabbitMqConfig.SALES_QUEUE);
            salesConsumer.register("PaymentRegisteredEvent", new SalesDepositListener(sales));
            salesConsumer.start();

            // --- BILLING AND SETTLEMENT ---
            EventPublisher billingPublisher = new RabbitMqEventPublisherAdapter(connection, serializer);
            InMemorySettlementRepository settlementRepo = new InMemorySettlementRepository();
            InMemoryDocumentRepository documentRepo = new InMemoryDocumentRepository();

            PaymentProcessService settlements = new PaymentProcessService(
                    settlementRepo, new SettlementFactory(), documentRepo,
                    new NotificationMockAdapter(), billingPublisher);

            SettlementEventListener listener = new SettlementEventListener(settlements);
            listener.on(new OrderReadyForSettlementEvent(
                    UUID.randomUUID(), orderId, new BigDecimal("100000"), "PLN", Instant.now()));

            DocumentGenerationService docs = new DocumentGenerationService(
                    settlementRepo, documentRepo,
                    new InvoiceCalculationService(), new AccountingDocumentFactory(),
                    new PdfGeneratorMockAdapter(), new NotificationMockAdapter(), billingPublisher,
                    new SalesCrmIntegrationAdapter(new SalesQueryService(orderRepo, offerRepo, customerRepo)),
                    new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"));

            System.out.println(">> generateAdvance(" + orderId + ") -> AdvancePaymentRequestedEvent (UC-FIR-01)...");
            docs.generateAdvance(new GenerateAdvanceCommand(orderId, "accountant@salon.pl"));

            System.out.println(">> processPayment (deposit) -> PaymentRegisteredEvent on the broker...");
            settlements.processPayment(new ProcessPaymentCommand(
                    orderId, "TX-DEMO-1", new BigDecimal("10000"), "PLN"));

            // Give the consumer a moment to receive the message from the queue.
            Thread.sleep(1500);
            System.out.println(">> Order state after the event passed through the queue: "
                    + orderRepo.findById(new OrderId(orderId)).get().getState());
        }
    }
}
