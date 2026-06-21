package salon.bootstrap;

import salon.billing.application.command.GenerateAdvanceCommand;
import salon.billing.application.command.ProcessPaymentCommand;
import salon.billing.application.service.DocumentGenerationService;
import salon.billing.application.service.PaymentProcessService;
import salon.billing.application.domain.event.AdvancePaymentRegisteredEvent;
import salon.billing.application.domain.event.AdvancePaymentRequestedEvent;
import salon.billing.application.domain.event.PaymentRegisteredEvent;
import salon.billing.application.domain.event.SettlementCompletedEvent;
import salon.billing.application.domain.model.document.AccountingDocumentFactory;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.service.InvoiceCalculationService;
import salon.billing.infrastructure.out.integration.SalesCrmIntegrationAdapter;
import salon.billing.infrastructure.out.mock.InMemoryDocumentRepository;
import salon.billing.infrastructure.out.mock.InMemorySettlementRepository;
import salon.billing.infrastructure.out.mock.NotificationMockAdapter;
import salon.billing.infrastructure.out.mock.PdfGeneratorMockAdapter;
import salon.logistics.application.service.InventoryManagementService;
import salon.logistics.application.domain.event.FactoryOrderPlacedEvent;
import salon.logistics.application.domain.event.VehicleDeliveredToStockEvent;
import salon.logistics.application.domain.event.VehicleInventoryReleasedEvent;
import salon.logistics.application.domain.event.VehicleIsNotOnStockEvent;
import salon.logistics.application.domain.event.VehicleReadyForHandoverEvent;
import salon.logistics.infrastructure.out.mock.FactoryIntegrationMockAdapter;
import salon.logistics.infrastructure.out.mock.InMemoryInventoryRepository;
import salon.logistics.infrastructure.out.mock.InMemorySpecificationReadModelAdapter;
import salon.sales.application.command.ScheduleHandoverCommand;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.service.SalesService;
import salon.sales.application.service.SalesQueryService;
import salon.sales.application.domain.event.OrderPlacedEvent;
import salon.sales.application.domain.model.customer.Address;
import salon.sales.application.domain.model.customer.ContactData;
import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.infrastructure.out.integration.InventoryCommandAdapter;
import salon.sales.infrastructure.out.mock.InMemoryCustomerRepository;
import salon.sales.infrastructure.out.mock.InMemoryOfferRepository;
import salon.sales.infrastructure.out.mock.InMemoryOrderRepository;
import salon.sales.infrastructure.out.mock.InMemorySpecificationPriceReadModelAdapter;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Demo (offline, in-process): the full "Long Track" choreography per the PDF —
 * UC-CRM-01..05 + UC-INW-01/02/03/05/06 + UC-FIR-01/03.
 *
 * Flow: configurator -> proforma offer (pricing from the Catalog) -> acceptance
 * -> no car in the yard -> deposit request -> deposit payment (order activation
 * + production order) -> delivery to the yard -> balance top-up (SettlementCompleted
 * -> ready for handover) -> scheduling the handover -> vehicle handover (ReleaseVehicle).
 */
public class SalonDemo {

    public static void main(String[] args) {
        InProcessChoreographyBus bus = new InProcessChoreographyBus();

        // --- INWENTARZ I LOGISTYKA ---
        InMemoryInventoryRepository inventoryRepo = new InMemoryInventoryRepository();
        InventoryManagementService inventory = new InventoryManagementService(
                inventoryRepo, new InMemorySpecificationReadModelAdapter(),
                new FactoryIntegrationMockAdapter(), bus);

        // --- SALES AND CRM ---
        InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
        InMemoryOfferRepository offerRepo = new InMemoryOfferRepository();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        SalesService sales = new SalesService(
                customerRepo, offerRepo, orderRepo, bus,
                new InMemorySpecificationPriceReadModelAdapter(),            // pricing read model (UC-CRM-02)
                new InventoryCommandAdapter(inventory, inventory, inventoryRepo),
                null);                                                       // billing via events

        // --- BILLING AND SETTLEMENT ---
        InMemorySettlementRepository settlementRepo = new InMemorySettlementRepository();
        InMemoryDocumentRepository documentRepo = new InMemoryDocumentRepository();
        PaymentProcessService settlements = new PaymentProcessService(
                settlementRepo, new SettlementFactory(), documentRepo,
                new NotificationMockAdapter(), bus);
        DocumentGenerationService documents = new DocumentGenerationService(
                settlementRepo, documentRepo,
                new InvoiceCalculationService(), new AccountingDocumentFactory(),
                new PdfGeneratorMockAdapter(), new NotificationMockAdapter(), bus,
                new SalesCrmIntegrationAdapter(new SalesQueryService(orderRepo, offerRepo, customerRepo)),
                new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"));

        // --- Wiring the choreography (subscriptions as on the context canvases) ---
        bus.sales = sales;
        bus.inventory = inventory;
        bus.documents = documents;
        bus.settlements = settlements;
        bus.orderRepo = orderRepo;

        // ===== Scenariusz =====
        // Simulation of the SpecificationCompleted event from the Catalog (the demo skips the configurator):
        // Inventory receives the equipment codes asynchronously and builds a local read model.
        inventory.registerSpecification("SPEC-1",
                List.of("ENG-HYBRID", "COL-RED", "PKG-COMFORT"));
        // Sales receives the computed catalog price via the same event (pricing read model).
        sales.registerSpecificationPrice("SPEC-1", Money.of(100000, "PLN"));
        sales.registerCustomer(new Customer(new CustomerId("CUST-1"), "Jan Kowalski", "1234563218",
                new Address("Main Street 1", "00-001", "Warsaw", "PL"),
                new ContactData("jan.kowalski@example.com", "+48 600 100 200")));

        System.out.println("=== UC-CRM-01: uruchomienie sesji konfiguratora ===");
        String sessionId = sales.startConfiguratorSession(
                new StartConfiguratorSessionCommand("CUST-1", "SP-7"));
        System.out.println("Sesja: " + sessionId);

        System.out.println("\n=== UC-CRM-02: proforma offer (pricing from the Catalog: 100 000 PLN) ===");
        OfferId offerId = sales.generateOffer("CUST-1", "SPEC-1");
        System.out.println("Offer " + offerId.value() + ": "
                + offerRepo.findById(offerId).get().state());

        System.out.println("\n=== UC-CRM-03: offer acceptance and order creation ===");
        String orderId = sales.acceptOfferAndCreateOrder(offerId);
        System.out.println("Order " + orderId + ": "
                + orderRepo.findById(new OrderId(orderId)).get().state());

        System.out.println("\n=== UC-FIR-03: deposit payment (10% = 10 000 PLN) ===");
        settlements.processPayment(new ProcessPaymentCommand(
                orderId, "TX-1", new BigDecimal("10000.00"), "PLN"));
        System.out.println("Order after deposit: "
                + orderRepo.findById(new OrderId(orderId)).get().state());

        System.out.println("\n=== UC-INW-03: vehicle delivery from the factory to the yard ===");
        String vin = inventoryRepo.findByOrderId(new OrderId(orderId)).get().vin().value();
        inventory.receiveVehicle(vin);

        System.out.println("\n=== UC-FIR-03: top-up of the remaining balance ===");
        settlements.processPayment(new ProcessPaymentCommand(
                orderId, "TX-2", new BigDecimal("90000.00"), "PLN"));

        System.out.println("\n=== UC-CRM-04: scheduling the handover ===");
        sales.scheduleHandover(new ScheduleHandoverCommand(orderId, LocalDate.now().plusDays(3)));
        System.out.println("Order: " + orderRepo.findById(new OrderId(orderId)).get().state());

        System.out.println("\n=== UC-CRM-05: registering the physical vehicle handover ===");
        sales.confirmHandover(new OrderId(orderId));
        System.out.println("Order: " + orderRepo.findById(new OrderId(orderId)).get().state());
        System.out.println("Vehicle:    " + inventoryRepo.findByVin(
                inventoryRepo.findAll().get(0).vin()).get().state());

        System.out.println("\n[OK] Full PDF cycle (Long Track) completed.");
    }

    /**
     * In-process bus: dispatches domain events to subscribers in other contexts
     * exactly as on the canvases (Inbound/Outbound Communication).
     */
    private static final class InProcessChoreographyBus implements EventPublisher {

        SalesService sales;
        InventoryManagementService inventory;
        DocumentGenerationService documents;
        PaymentProcessService settlements;
        InMemoryOrderRepository orderRepo;

        private final List<DomainEvent> pending = new ArrayList<>();
        private boolean dispatching = false;

        @Override
        public void publish(DomainEvent event) {
            System.out.println("   [bus] -> " + event.getClass().getSimpleName());
            this.pending.add(event);
            if (this.dispatching) {
                return; // the event will be handled by the outer loop (FIFO order, no recursion)
            }
            this.dispatching = true;
            try {
                while (!this.pending.isEmpty()) {
                    dispatch(this.pending.remove(0));
                }
            } finally {
                this.dispatching = false;
            }
        }

        private void dispatch(DomainEvent event) {
            // Sales -> Inventory: OrderPlaced carries specificationId -> linkage in the read model
            // (event-carried state transfer; UC-INW-01/02 then read only local data).
            if (event instanceof OrderPlacedEvent e && inventory != null
                    && e.specificationId() != null) {
                inventory.linkOrderToSpecification(e.orderId(), e.specificationId());
            }
            // Sales -> Billing: new order -> balance initialization (contract value).
            if (event instanceof OrderPlacedEvent e && settlements != null && orderRepo != null) {
                orderRepo.findById(new OrderId(e.orderId())).ifPresent(order ->
                        settlements.initializeSettlement(order.id(), order.requiredDeposit()));
            }
            // Inventory -> Billing: no car -> deposit request (UC-FIR-01).
            if (event instanceof VehicleIsNotOnStockEvent e && documents != null) {
                documents.generateAdvance(new GenerateAdvanceCommand(e.orderId(), "accountant@salon.pl"));
            }
            // Billing -> Sales: payment posted -> order activation (UC-CRM-03 part 2).
            if (event instanceof PaymentRegisteredEvent e && sales != null) {
                sales.activateOnDeposit(e.orderId());
            }
            // Billing -> Inventory: deposit posted -> production order (UC-INW-02).
            if (event instanceof AdvancePaymentRegisteredEvent e && inventory != null) {
                inventory.orderVehicleFromFactory(e.orderId());
            }
            // Billing -> Inventory: balance = 0 -> preparation for handover (UC-INW-05).
            if (event instanceof SettlementCompletedEvent e && inventory != null) {
                inventory.prepareVehicleForHandover(e.orderId());
            }
            // Inventory -> Sales: vehicle ready -> "Ready for handover" + VIN assignment (UC-CRM-04).
            if (event instanceof VehicleReadyForHandoverEvent e && sales != null) {
                sales.markOrderAsReadyForHandover(new OrderId(e.orderId()));
                orderRepo.findById(new OrderId(e.orderId())).ifPresent(order -> {
                    order.assignVehicle(e.vin());
                    orderRepo.save(order);
                });
            }
            // Informational events:
            if (event instanceof AdvancePaymentRequestedEvent e) {
                System.out.println("   [CRM] Customer of order " + e.orderId()
                        + " asked for a deposit (e-mail with transfer details).");
            }
            if (event instanceof FactoryOrderPlacedEvent e) {
                System.out.println("   [INW] Production order accepted, VIN=" + e.vin() + ".");
            }
            if (event instanceof VehicleDeliveredToStockEvent e) {
                System.out.println("   [INW] Vehicle " + e.vin() + " delivered and matched with "
                        + e.orderId() + ".");
            }
            if (event instanceof VehicleInventoryReleasedEvent e) {
                System.out.println("   [INW] Vehicle " + e.vin() + " removed from stock (HANDED_OVER).");
            }
        }
    }
}
