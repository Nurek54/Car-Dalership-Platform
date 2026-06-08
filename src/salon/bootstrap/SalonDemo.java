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
import salon.shared.event.DomainEvent;
import salon.shared.infrastructure.messaging.EventJson;
import salon.shared.infrastructure.messaging.RecordEventSerializer;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Demo IN-PROCESS (bez RabbitMQ) pokazujące choreografię między kontekstami
 * oraz wzorzec "agregat generuje zdarzenia, aplikacja je ściąga i publikuje".
 *
 * Przepływ:
 *   FAKTUROWANIE: generateAdvance -> Settlement.requestAdvancePayment() SAM rejestruje
 *      AdvancePaymentRequestedEvent -> serwis ściąga zdarzenie (pullDomainEvents) i publikuje
 *      na magistralę -> magistrala (in-process) serializuje je jak RabbitMQ i kieruje do SPRZEDAŻY
 *      -> SalesDepositListener -> OrderAppService aktywuje zamówienie.
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

        // --- FAKTUROWANIE I ROZLICZENIA ---
        InMemorySettlementRepository settlementRepo = new InMemorySettlementRepository();
        SettlementAppService settlements = new SettlementAppService(
                settlementRepo, new SettlementFactory(), bus, new PaymentGatewayMockAdapter());

        // Inicjalizacja salda dla ORD-1 (kontrakt 100 000 PLN).
        SettlementEventListener listener = new SettlementEventListener(settlements);
        listener.on(new OrderReadyForSettlementEvent(
                UUID.randomUUID(), "ORD-1", new BigDecimal("100000"), "PLN", Instant.now()));

        DocumentAppService docs = new DocumentAppService(
                settlementRepo, new InMemoryDocumentRepository(),
                new InvoiceCalculationDomainService(), new AccountingDocumentFactory(),
                new PdfGeneratorMockAdapter(), new NotificationMockAdapter(), bus,
                new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"));

        System.out.println("=== Choreografia: żądanie zadatku -> aktywacja zamówienia ===");
        System.out.println("Stan zamówienia PRZED: "
                + orderRepo.findById(new OrderId("ORD-1")).get().getState());

        System.out.println("\n-> generateAdvance(ORD-1) (UC-FIR-01)");
        docs.generateAdvance(new GenerateAdvanceCommand(
                "ORD-1", "Jan Kowalski", "1234567890", "ksiegowy@salon.pl"));

        System.out.println("\nStan zamówienia PO:    "
                + orderRepo.findById(new OrderId("ORD-1")).get().getState());

        System.out.println("\n[OK] Demo zakończone. Zdarzenie powstało W AGREGACIE, "
                + "serwis je tylko ściągnął i opublikował.");
    }

    /**
     * Magistrala in-process: serializuje zdarzenie dokładnie jak adapter RabbitMQ
     * (ten sam RecordEventSerializer), a potem zamiast wysyłać do brokera — kieruje
     * AdvancePaymentRequestedEvent do listenera Sprzedaży. Pozostałe zdarzenia tylko loguje.
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

            if (this.salesListener != null && "AdvancePaymentRequestedEvent".equals(type)) {
                this.salesListener.handle(EventJson.read(json));
            }
        }
    }
}
