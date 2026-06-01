package salon.bootstrap;

import salon.billing.application.port.in.RegisterPaymentCommand;
import salon.billing.application.service.PaymentAppService;
import salon.billing.domain.service.PaymentClassificationService;
import salon.billing.infrastructure.mock.InMemoryPaymentRepository;
import salon.billing.infrastructure.mock.PaymentGatewayMockAdapter;
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

/**
 * Demo PRZEZ KOLEJKĘ (wymaga działającego RabbitMQ na localhost:5672).
 *
 * Przepływ między kontekstami (to jest sedno wymagania #1/#2):
 *   ROZLICZENIA: rejestracja zadatku -> publikacja DepositRegisteredEvent na RabbitMQ
 *      -> kolejka sales.inbox -> SPRZEDAŻ (SalesDepositListener) aktywuje zamówienie
 *      -> publikacja OrderActivatedEvent.
 *
 * Najpierw odpal broker, np.:
 *   docker run -d --name salon-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
 */
public class MessagingDemo {

    public static void main(String[] args) throws Exception {
        try (RabbitMqConnection connection = new RabbitMqConnection()) {
            RecordEventSerializer serializer = new RecordEventSerializer();

            // --- SPRZEDAŻ: przygotowujemy zamówienie czekające na zadatek + uruchamiamy konsumenta ---
            InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
            EventPublisherPort salesPublisher = new RabbitMqEventPublisherAdapter(connection, serializer);
            OrderAppService orderService = new OrderAppService(orderRepo, salesPublisher);

            Order order = new Order(new OrderId("ORD-DEMO-1"), new OfferId("OFF-DEMO"));
            order.confirmSignature("SIGN-REF-DEMO"); // -> PENDING_PAYMENT
            orderRepo.save(order);

            RabbitMqEventConsumer salesConsumer =
                    new RabbitMqEventConsumer(connection, RabbitMqConfig.SALES_QUEUE);
            salesConsumer.register("DepositRegisteredEvent", new SalesDepositListener(orderService));
            salesConsumer.start();

            // --- ROZLICZENIA: rejestrujemy zadatek -> publikacja na RabbitMQ ---
            EventPublisherPort billingPublisher = new RabbitMqEventPublisherAdapter(connection, serializer);
            PaymentAppService payments = new PaymentAppService(
                    new InMemoryPaymentRepository(), billingPublisher,
                    new PaymentGatewayMockAdapter(), new PaymentClassificationService());

            System.out.println(">> Rejestruję zadatek 20000 PLN dla ORD-DEMO-1 ...");
            payments.registerPayment(new RegisterPaymentCommand(
                    "ORD-DEMO-1", new BigDecimal("20000"), "PLN", new BigDecimal("100000"), "TX-DEMO"));

            // Dajemy konsumentowi chwilę na odebranie wiadomości z kolejki.
            Thread.sleep(1500);
            System.out.println(">> Stan zamówienia po przejściu zdarzenia przez kolejkę: "
                    + orderRepo.findById(new OrderId("ORD-DEMO-1")).get().getState());
        }
    }
}
