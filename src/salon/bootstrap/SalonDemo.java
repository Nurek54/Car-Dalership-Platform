package salon.bootstrap;

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
import salon.sales.application.service.SalesService;
import salon.sales.infrastructure.in.messaging.BillingEventSubscriberAdapter;
import salon.sales.infrastructure.in.messaging.CatalogEventSubscriberAdapter;
import salon.sales.infrastructure.in.messaging.InventoryEventSubscriberAdapter;
import salon.sales.infrastructure.out.integration.BillingIntegrationAdapter;
import salon.sales.infrastructure.out.integration.CatalogIntegrationAdapter;
import salon.sales.infrastructure.out.integration.InventoryIntegrationAdapter;
import salon.sales.infrastructure.out.persistence.InMemoryCustomerRepository;
import salon.sales.infrastructure.out.persistence.InMemoryOfferRepository;
import salon.sales.infrastructure.out.persistence.InMemoryOrderRepository;
import salon.common.application.EventPublisher;
import salon.common.model.OrderId;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalonDemo {

    public static void main(String[] args) {
        EventPublisher bus = event -> System.out.println("   [bus] -> " + event.getClass().getSimpleName());

        InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
        InMemoryOfferRepository offerRepo = new InMemoryOfferRepository();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();

        SalesService sales = new SalesService(customerRepo, offerRepo, orderRepo,
                new BillingIntegrationAdapter(), new InventoryIntegrationAdapter(), bus,
                new OfferFactory(), new OrderFactory());
        ConfiguratorAppService configurator =
                new ConfiguratorAppService(new CatalogIntegrationAdapter(), bus);

        CatalogEventSubscriberAdapter catalogSubscriber = new CatalogEventSubscriberAdapter(sales);
        BillingEventSubscriberAdapter billingSubscriber = new BillingEventSubscriberAdapter(sales);
        InventoryEventSubscriberAdapter inventorySubscriber = new InventoryEventSubscriberAdapter(sales);

        sales.registerCustomer(new Customer(new CustomerId("CUST-1"), "Jan Kowalski", "1234563218",
                new Address("Main Street 1", "00-001", "Warsaw", "PL"),
                new ContactData("jan.kowalski@example.com", "+48 600 100 200")));

        System.out.println("=== UC-CRM-01: start configurator session ===");
        String sessionId = configurator.startSession(
                new StartConfiguratorSessionCommand("CUST-1", "SP-7", 2026));
        System.out.println("Session: " + sessionId);

        System.out.println("\n=== UC-CRM-02: proforma offer (price from the Catalog: 100 000 PLN) ===");
        catalogSubscriber.handleSpecificationCompleted(
                new CatalogEventSubscriberAdapter.SpecificationCompleted(
                        "CUST-1", "SPEC-1", new BigDecimal("100000"), "PLN"));
        Offer offer = offerRepo.findAll().get(0);
        System.out.println("Offer " + offer.getId().value() + ": " + offer.getState());

        System.out.println("\n=== UC-CRM-03: offer acceptance and order creation (bank transfer) ===");
        String orderId = sales.acceptOffer(
                new AcceptOfferCommand(offer.getId().value(), PaymentMethod.BANK_TRANSFER));
        System.out.println("Order " + orderId + ": "
                + orderRepo.findById(new OrderId(orderId)).get().getState());

        System.out.println("\n=== UC-CRM-03 (part 2): deposit registered by Billing -> activate order ===");
        billingSubscriber.handleAdvancePaymentRegistered(
                new BillingEventSubscriberAdapter.AdvancePaymentRegistered(orderId));

        System.out.println("\n=== UC-CRM-04: vehicle ready for handover (from Inventory) -> schedule ===");
        inventorySubscriber.handleVehicleReadyForHandover(
                new InventoryEventSubscriberAdapter.VehicleReadyForHandover(orderId));
        System.out.println("Order: " + orderRepo.findById(new OrderId(orderId)).get().getState());
        sales.scheduleHandover(new ScheduleHandoverCommand(orderId, LocalDate.now().plusDays(3)));
        System.out.println("Order: " + orderRepo.findById(new OrderId(orderId)).get().getState());

        System.out.println("\n=== UC-CRM-05: registering the physical vehicle handover ===");
        sales.releaseVehicle(orderId);
        System.out.println("Order: " + orderRepo.findById(new OrderId(orderId)).get().getState());

        System.out.println("\n[OK] Sales & CRM long track (UC-CRM-01..05) completed.");
    }
}
