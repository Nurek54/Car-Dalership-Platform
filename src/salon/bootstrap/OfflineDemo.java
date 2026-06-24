package salon.bootstrap;

import salon.common.application.EventPublisher;
import salon.common.model.OrderId;

import salon.catalog.application.domain.exception.CombinationNotAllowedException;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;
import salon.catalog.application.domain.service.RuleValidationService;

import salon.sales.application.command.AcceptOfferCommand;
import salon.sales.application.command.ScheduleHandoverCommand;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.domain.model.customer.Address;
import salon.sales.application.domain.model.customer.ContactData;
import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferFactory;
import salon.sales.application.domain.model.order.OrderFactory;
import salon.sales.application.domain.model.order.PaymentMethod;
import salon.sales.application.service.ConfiguratorAppService;
import salon.sales.application.service.SalesQueryService;
import salon.sales.application.service.SalesService;
import salon.sales.infrastructure.in.messaging.BillingEventSubscriberAdapter;
import salon.sales.infrastructure.in.messaging.CatalogEventSubscriberAdapter;
import salon.sales.infrastructure.in.messaging.InventoryEventSubscriberAdapter;
import salon.sales.infrastructure.out.integration.BillingIntegrationAdapter;
import salon.sales.infrastructure.out.integration.InventoryIntegrationAdapter;
import salon.sales.infrastructure.out.persistence.InMemoryCustomerRepository;
import salon.sales.infrastructure.out.persistence.InMemoryOfferRepository;
import salon.sales.infrastructure.out.persistence.InMemoryOrderRepository;

import salon.logistics.application.service.InventoryManagementService;
import salon.logistics.infrastructure.out.mock.FactoryIntegrationMockAdapter;
import salon.logistics.infrastructure.out.mock.InMemoryInventoryRepository;
import salon.logistics.infrastructure.out.mock.InMemorySpecificationReadModelAdapter;

import salon.billing.application.command.GenerateInvoiceCommand;
import salon.billing.application.command.ProcessPaymentCommand;
import salon.billing.application.domain.model.document.AccountingDocumentFactory;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.service.InvoiceCalculationService;
import salon.billing.application.service.DocumentGenerationService;
import salon.billing.application.service.PaymentProcessService;
import salon.billing.infrastructure.in.messaging.OrderReadyForSettlementEvent;
import salon.billing.infrastructure.in.messaging.SettlementEventListener;
import salon.billing.infrastructure.out.mock.InMemoryDocumentRepository;
import salon.billing.infrastructure.out.mock.InMemorySettlementRepository;
import salon.billing.infrastructure.out.mock.NotificationMockAdapter;
import salon.billing.infrastructure.out.mock.PdfGeneratorMockAdapter;

import salon.financing.application.domain.model.financing.FinancingApplicationFactory;
import salon.financing.application.service.ProcessFinancingService;
import salon.financing.infrastructure.in.messaging.FinancingEventListener;
import salon.financing.infrastructure.out.mock.BankIntegrationMockAdapter;
import salon.financing.infrastructure.out.mock.InMemoryFinancingRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * OfflineDemo - the full "from configuration to invoice" business flow of the car dealership,
 * run locally (no RabbitMQ, on in-memory repositories).
 *
 * The demo drives ONE order through all bounded contexts:
 *   Catalog -> Sales -> Billing -> Logistics -> Sales (handover) -> Invoice
 * and finally shows the financing variant (the Financing context).
 *
 * Domain events (the communication between contexts) are printed to the console
 * on lines "-> event: ...". Just read the output from top to bottom.
 */
public class OfflineDemo {

    public static void main(String[] args) {

        // Shared event bus: here it simply prints every domain event,
        // so you can see what comes out of each context and when.
        EventPublisher bus = event -> System.out.println("   -> event: " + event.getClass().getSimpleName());

        // --- Shared sales repositories (customer / offer / order) ---
        InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
        InMemoryOfferRepository offerRepo = new InMemoryOfferRepository();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();

        // --- Application services and inbound adapters (in-memory wiring) ---
        SalesService sales = new SalesService(customerRepo, offerRepo, orderRepo,
                new BillingIntegrationAdapter(), new InventoryIntegrationAdapter(), bus,
                new OfferFactory(), new OrderFactory());
        ConfiguratorAppService configurator = new ConfiguratorAppService(customerRepo, bus);

        CatalogEventSubscriberAdapter fromCatalog = new CatalogEventSubscriberAdapter(sales);
        BillingEventSubscriberAdapter fromBilling = new BillingEventSubscriberAdapter(sales);
        InventoryEventSubscriberAdapter fromInventory = new InventoryEventSubscriberAdapter(sales);

        InMemoryInventoryRepository vehicleRepo = new InMemoryInventoryRepository();
        InventoryManagementService inventory = new InventoryManagementService(
                vehicleRepo,
                new InMemorySpecificationReadModelAdapter(),
                new FactoryIntegrationMockAdapter(),
                bus);

        // =================================================================
        section("STEP 1 - CATALOG: vehicle configuration and compatibility rules");
        // =================================================================
        ProductCatalog catalog = new ProductCatalogFactory().createNew(
                ModelYear.of(2026),
                List.of(
                        new CatalogOption(OptionCode.of("MANUAL_GEARBOX"), Money.zero("PLN")),
                        new CatalogOption(OptionCode.of("ADAPTIVE_CRUISE"),
                                Money.of(new BigDecimal("3000"), "PLN"))),
                List.of(
                        new CatalogRule(OptionCode.of("ADAPTIVE_CRUISE"),
                                OptionCode.of("MANUAL_GEARBOX"), RuleType.EXCLUDES)));

        VehicleSpecification spec = new VehicleSpecificationFactory()
                .createDraft(catalog.id(), catalog.currencyCode());
        spec.addOption(OptionCode.of("MANUAL_GEARBOX"), catalog);
        System.out.println("Added option MANUAL_GEARBOX.");
        try {
            spec.addOption(OptionCode.of("ADAPTIVE_CRUISE"), catalog);
            new RuleValidationService().validateSelection(spec, catalog);
        } catch (CombinationNotAllowedException e) {
            System.out.println("Rule fired (fail-fast): " + e.getMessage());
        }
        System.out.println("Catalog priced the final configuration at 100,000 PLN.");

        // =================================================================
        section("STEP 2 - SALES: customer, configurator session and proforma offer");
        // =================================================================
        sales.registerCustomer(new Customer(new CustomerId("CUST-1"), "Jan Kowalski", "1234563218",
                new Address("Main Street 1", "00-001", "Warsaw", "PL"),
                new ContactData("jan.kowalski@example.com", "+48 600 100 200")));
        System.out.println("Registered customer CUST-1 (Jan Kowalski).");

        String sessionId = configurator.startSession(new StartConfiguratorSessionCommand("CUST-1", "SP-7"));
        System.out.println("Started configurator session: " + sessionId);

        // Catalog finished the specification -> an event reaches Sales, which creates the offer.
        fromCatalog.handleSpecificationCompleted(new CatalogEventSubscriberAdapter.SpecificationCompleted(
                "CUST-1", "SPEC-1", new BigDecimal("100000"), "PLN"));
        Offer offer = offerRepo.findAll().get(0);
        System.out.println("Created offer " + offer.id().value() + " in state " + offer.state() + ".");

        // =================================================================
        section("STEP 3 - SALES: offer acceptance and order placement");
        // =================================================================
        String orderId = sales.acceptOffer(new AcceptOfferCommand(offer.id().value(), PaymentMethod.BANK_TRANSFER));
        System.out.println("Order " + orderId + " in state "
                + orderRepo.findById(new OrderId(orderId)).get().state()
                + " (payment: bank transfer).");

        // =================================================================
        section("STEP 4 - BILLING: opening the settlement, deposit and full payment");
        // =================================================================
        InMemorySettlementRepository settlementRepo = new InMemorySettlementRepository();
        InMemoryDocumentRepository documentRepo = new InMemoryDocumentRepository();
        PaymentProcessService settlements = new PaymentProcessService(
                settlementRepo, new SettlementFactory(), documentRepo, new NotificationMockAdapter(), bus);
        SettlementEventListener settlementListener = new SettlementEventListener(settlements);

        // Sales hands the order over for settlement -> Billing opens the account.
        settlementListener.on(new OrderReadyForSettlementEvent(
                UUID.randomUUID(), orderId, new BigDecimal("100000"), "PLN", Instant.now()));
        System.out.println("Opened settlement for order " + orderId + ".");

        // Deposit of 20,000 PLN -> Billing confirms the payment -> Sales keeps the order active.
        settlements.processPayment(new ProcessPaymentCommand(orderId, "TX-1", new BigDecimal("20000"), "PLN"));
        fromBilling.handleAdvancePaymentRegistered(new BillingEventSubscriberAdapter.AdvancePaymentRegistered(orderId));
        System.out.println("Deposit of 20,000 PLN booked.");

        // Remaining 80,000 PLN -> full settlement.
        settlements.processPayment(new ProcessPaymentCommand(orderId, "TX-2", new BigDecimal("80000"), "PLN"));
        System.out.println("Remaining 80,000 PLN booked - balance settled (SETTLED).");

        // =================================================================
        section("STEP 5 - LOGISTICS: reservation, factory order and vehicle preparation");
        // =================================================================
        inventory.registerSpecification("SPEC-1", List.of("MANUAL_GEARBOX"));
        inventory.linkOrderToSpecification(orderId, "SPEC-1");

        // No vehicle in stock -> it has to be ordered from the factory.
        inventory.reserveVehicleForOrder(orderId);
        System.out.println("No vehicle on the lot - ordering from the factory.");
        inventory.orderVehicleFromFactory(orderId);
        String vin = vehicleRepo.findByOrderId(new OrderId(orderId)).orElseThrow().vin().value();
        System.out.println("Factory accepted the order, VIN: " + vin + ".");

        // The vehicle arrives at the lot and is then prepared for handover.
        inventory.receiveVehicle(vin);
        System.out.println("Vehicle " + vin + " received on the lot.");
        inventory.prepareVehicleForHandover(orderId);
        System.out.println("Vehicle prepared - ready for handover.");

        // =================================================================
        section("STEP 6 - SALES: scheduling and confirming the handover");
        // =================================================================
        // Logistics reports readiness -> Sales moves the order to READY_FOR_HANDOVER.
        fromInventory.handleVehicleReadyForHandover(
                new InventoryEventSubscriberAdapter.VehicleReadyForHandover(orderId));
        System.out.println("Order: " + orderRepo.findById(new OrderId(orderId)).get().state() + ".");

        sales.scheduleHandover(new ScheduleHandoverCommand(orderId, LocalDate.now().plusDays(3)));
        System.out.println("Handover scheduled: " + orderRepo.findById(new OrderId(orderId)).get().state() + ".");

        sales.releaseVehicle(orderId);
        System.out.println("Vehicle handed over to the customer: " + orderRepo.findById(new OrderId(orderId)).get().state() + ".");

        // =================================================================
        section("STEP 7 - BILLING: issuing the final invoice");
        // =================================================================
        DocumentGenerationService documents = new DocumentGenerationService(
                settlementRepo, documentRepo,
                new InvoiceCalculationService(), new AccountingDocumentFactory(),
                new PdfGeneratorMockAdapter(), new NotificationMockAdapter(), bus,
                new salon.billing.infrastructure.out.integration.SalesCrmIntegrationAdapter(
                        new SalesQueryService(orderRepo, offerRepo, customerRepo)),
                new SellerDetails("Car Dealership Ltd.", "5260000000"));

        String invoiceId = documents.generateInvoice(new GenerateInvoiceCommand(
                orderId, "Final invoice " + orderId, "accountant@salon.pl"));
        System.out.println("Invoice issued, id = " + invoiceId + ".");

        // =================================================================
        section("VARIANT - FINANCING: a second customer buys the vehicle on credit");
        // =================================================================
        sales.registerCustomer(new Customer(new CustomerId("CUST-2"), "Anna Nowak", "5252248481",
                new Address("Park Avenue 5", "00-002", "Cracow", "PL"),
                new ContactData("anna.nowak@example.com", "+48 600 300 400")));
        fromCatalog.handleSpecificationCompleted(new CatalogEventSubscriberAdapter.SpecificationCompleted(
                "CUST-2", "SPEC-2", new BigDecimal("150000"), "PLN"));
        Offer offer2 = offerRepo.findAll().stream()
                .filter(o -> o.customerId().value().equals("CUST-2"))
                .findFirst().orElseThrow();
        String order2 = sales.acceptOffer(new AcceptOfferCommand(offer2.id().value(), PaymentMethod.FINANCING));
        System.out.println("Order " + order2 + " placed with payment: financing.");

        ProcessFinancingService financing = new ProcessFinancingService(
                new InMemoryFinancingRepository(), new FinancingApplicationFactory(),
                new salon.financing.infrastructure.out.integration.SalesCrmIntegrationAdapter(
                        new SalesQueryService(orderRepo, offerRepo, customerRepo)),
                new BankIntegrationMockAdapter(), bus);
        FinancingEventListener fromSalesToFinancing = new FinancingEventListener(financing);

        // Sales requests financing -> the application goes to the bank.
        fromSalesToFinancing.handleFinancingRequested(
                new FinancingEventListener.FinancingRequested(order2, "CUST-2"));
        System.out.println("Financing application submitted for order " + order2 + ".");

        // The bank returns a positive decision -> financing approved.
        fromSalesToFinancing.handleFinancingDecisionReceived(
                new FinancingEventListener.FinancingDecisionReceivedFromBank(order2, true));
        System.out.println("Bank approved the financing - the order can proceed.");

        section("END - full flow completed successfully");
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("================================================================");
        System.out.println("  " + title);
        System.out.println("================================================================");
    }
}
