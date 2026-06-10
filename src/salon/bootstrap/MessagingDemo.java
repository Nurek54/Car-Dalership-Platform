package salon.bootstrap;

import salon.billing.application.port.in.GenerateAdvanceCommand;
import salon.billing.application.service.DocumentAppService;
import salon.billing.application.service.SettlementAppService;
import salon.billing.domain.model.document.AccountingDocumentFactory;
import salon.billing.domain.model.document.SellerDetails;
import salon.billing.domain.model.settlement.SettlementFactory;
import salon.billing.domain.service.InvoiceCalculationDomainService;
import salon.billing.infrastructure.messaging.OrderReadyForSettlementEvent;
import salon.billing.infrastructure.messaging.SettlementEventListener;
import salon.billing.infrastructure.mock.CrmIntegrationMockAdapter;
import salon.billing.infrastructure.mock.InMemoryDocumentRepository;
import salon.billing.infrastructure.mock.InMemorySettlementRepository;
import salon.billing.infrastructure.mock.NotificationMockAdapter;
import salon.billing.infrastructure.mock.PaymentGatewayMockAdapter;
import salon.billing.infrastructure.mock.PdfGeneratorMockAdapter;
import salon.sales.application.service.OrderAppService;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.order.Order;
import salon.sales.infrastructure.messaging.SalesDepositListener;
import salon.sales.infrastructure.mock.InMemoryOrderRepository;
import salon.shared.application.EventPublisherPort;
import salon.shared.infrastructure.messaging.RabbitMqConfig;
import salon.shared.infrastructure.messaging.RabbitMqConnection;
import salon.shared.infrastructure.messaging.RabbitMqEventConsumer;
import salon.shared.infrastructure.messaging.RabbitMqEventPublisherAdapter;
import salon.shared.infrastructure.messaging.RecordEventSerializer;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Demo PRZEZ KOLEJKĘ (wymaga działającego RabbitMQ na localhost:5672).
 *
 * Przepływ między kontekstami:
 *   FAKTUROWANIE: generateAdvance (UC-FIR-01) -> publikacja AdvancePaymentRequestedEvent na RabbitMQ
 *      -> kolejka sales.inbox -> SPRZEDAŻ (SalesDepositListener) aktywuje zamówienie.
 *
 * Najpierw odpal broker, np.:
 *   docker run -d --name salon-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
 */
public class MessagingDemo {

    public static void main(String[] args) throws Exception {
        try (RabbitMqConnection connection = new RabbitMqConnection()) {
            RecordEventSerializer serializer = new RecordEventSerializer();

            // --- SPRZEDAŻ: zamówienie czekające na zadatek + konsument ---
            InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
            EventPublisherPort salesPublisher = new RabbitMqEventPublisherAdapter(connection, serializer);
            OrderAppService orderService = new OrderAppService(orderRepo, salesPublisher);

            Order order = new Order(new OrderId("ORD-DEMO-1"), new OfferId("OFF-DEMO"));
            order.confirmSignature("SIGN-REF-DEMO"); // -> PENDING_PAYMENT
            orderRepo.save(order);

            RabbitMqEventConsumer salesConsumer =
                    new RabbitMqEventConsumer(connection, RabbitMqConfig.SALES_QUEUE);
            salesConsumer.register("AdvancePaymentRequestedEvent", new SalesDepositListener(orderService));
            salesConsumer.start();

            // --- FAKTUROWANIE I ROZLICZENIA ---
            EventPublisherPort billingPublisher = new RabbitMqEventPublisherAdapter(connection, serializer);
            InMemorySettlementRepository settlementRepo = new InMemorySettlementRepository();

            SettlementAppService settlements = new SettlementAppService(
                    settlementRepo, new SettlementFactory(), billingPublisher,
                    new PaymentGatewayMockAdapter());

            SettlementEventListener listener = new SettlementEventListener(settlements);
            listener.on(new OrderReadyForSettlementEvent(
                    UUID.randomUUID(), "ORD-DEMO-1", new BigDecimal("100000"), "PLN", Instant.now()));

            DocumentAppService docs = new DocumentAppService(
                    settlementRepo, new InMemoryDocumentRepository(),
                    new InvoiceCalculationDomainService(), new AccountingDocumentFactory(),
                    new PdfGeneratorMockAdapter(), new NotificationMockAdapter(), billingPublisher,
                    new CrmIntegrationMockAdapter(),
                    new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"));

            System.out.println(">> generateAdvance(ORD-DEMO-1) -> publikacja AdvancePaymentRequestedEvent ...");
            docs.generateAdvance(new GenerateAdvanceCommand("ORD-DEMO-1", "ksiegowy@salon.pl"));

            // Dajemy konsumentowi chwilę na odebranie wiadomości z kolejki.
            Thread.sleep(1500);
            System.out.println(">> Stan zamówienia po przejściu zdarzenia przez kolejkę: "
                    + orderRepo.findById(new OrderId("ORD-DEMO-1")).get().getState());
        }
    }
}
