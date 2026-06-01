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
import salon.shared.event.DomainEvent;
import salon.shared.infrastructure.messaging.EventJson;
import salon.shared.infrastructure.messaging.RecordEventSerializer;
import salon.shared.model.OrderId;

import java.math.BigDecimal;

/**
 * Demo IN-PROCESS (bez RabbitMQ) pokazujące CAŁĄ choreografię między kontekstami
 * oraz nowy wzorzec "agregat generuje zdarzenia, aplikacja je ściąga i publikuje".
 *
 * Przepływ:
 *   ROZLICZENIA: registerPayment -> Payment.categorizePayment() SAM rejestruje DepositRegisteredEvent
 *      -> serwis ściąga zdarzenie (pullDomainEvents) i publikuje na magistralę
 *      -> magistrala (in-process) serializuje je tak jak RabbitMQ i kieruje do SPRZEDAŻY
 *      -> SalesDepositListener -> OrderAppService.activateOnDeposit()
 *      -> Order.activate() SAM rejestruje OrderActivatedEvent -> serwis publikuje.
 *
 * Magistrala używa tych samych klas co produkcyjny adapter (RecordEventSerializer + EventJson),
 * tylko zamiast brokera woła listener bezpośrednio — dzięki temu nie trzeba Dockera.
 */
public class SalonDemo {

    public static void main(String[] args) {
        InProcessChoreographyBus bus = new InProcessChoreographyBus();

        // --- SPRZEDAŻ: zamówienie czekające na zadatek + listener aktywacji ---
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        OrderAppService orderService = new OrderAppService(orderRepo, bus);
        bus.wireSalesListener(new SalesDepositListener(orderService));

        Order order = new Order(new OrderId("ORD-1"), new OfferId("OFF-1"));
        order.confirmSignature("SIGN-REF-1"); // DRAFT_CREATED -> PENDING_PAYMENT
        orderRepo.save(order);

        // --- ROZLICZENIA: rejestracja zadatku ---
        PaymentAppService payments = new PaymentAppService(
                new InMemoryPaymentRepository(),
                bus,
                new PaymentGatewayMockAdapter(),
                new PaymentClassificationService());

        System.out.println("=== Choreografia: zadatek -> aktywacja zamówienia ===");
        System.out.println("Stan zamówienia PRZED: "
                + orderRepo.findById(new OrderId("ORD-1")).get().getState());

        System.out.println("\n-> registerPayment(ORD-1, 20000 PLN, wartość zamówienia 100000 PLN)");
        // wymagany zadatek = 10% z 100000 = 10000; wpłata 20000 >= 10000 -> ZADATEK
        payments.registerPayment(new RegisterPaymentCommand(
                "ORD-1", new BigDecimal("20000"), "PLN", new BigDecimal("100000"), null));

        System.out.println("\nStan zamówienia PO:    "
                + orderRepo.findById(new OrderId("ORD-1")).get().getState());

        System.out.println("\n=== Druga wpłata (zaliczka) — NIE aktywuje zamówienia ===");
        System.out.println("-> registerPayment(ORD-2, 5000 PLN, wartość zamówienia 100000 PLN)");
        // wpłata 5000 < wymagane 10000 -> ZALICZKA -> AdvanceRegisteredEvent (brak aktywacji)
        payments.registerPayment(new RegisterPaymentCommand(
                "ORD-2", new BigDecimal("5000"), "PLN", new BigDecimal("100000"), null));

        System.out.println("\n[OK] Demo zakończone. Zdarzenia powstały W AGREGATACH, "
                + "serwisy je tylko ściągnęły i opublikowały.");
    }

    /**
     * Magistrala in-process: serializuje zdarzenie dokładnie jak adapter RabbitMQ
     * (ten sam RecordEventSerializer), a potem zamiast wysyłać do brokera — kieruje
     * DepositRegisteredEvent do listenera Sprzedaży. Pozostałe zdarzenia tylko loguje.
     */
    private static final class InProcessChoreographyBus implements EventPublisherPort {

        private final RecordEventSerializer serializer = new RecordEventSerializer();
        private SalesDepositListener salesListener; // dowiązywany po zbudowaniu (cykl zależności)

        void wireSalesListener(SalesDepositListener listener) {
            this.salesListener = listener;
        }

        @Override
        public void publish(DomainEvent event) {
            String type = event.getClass().getSimpleName();
            String json = serializer.toJson(event);
            System.out.println("   [bus] -> " + json);

            if (this.salesListener != null && "DepositRegisteredEvent".equals(type)) {
                this.salesListener.handle(EventJson.read(json));
            }
        }
    }
}