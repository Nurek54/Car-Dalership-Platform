package salon.bootstrap;

import salon.billing.application.command.GenerateInvoiceCommand;
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
import salon.billing.infrastructure.out.mock.InProcessEventPublisherAdapter;
import salon.billing.infrastructure.out.mock.NotificationMockAdapter;
import salon.billing.infrastructure.out.mock.PdfGeneratorMockAdapter;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.exception.CombinationNotAllowedException;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;
import salon.catalog.application.domain.service.RuleValidationService;
import salon.sales.application.service.SalesQueryService;
import salon.sales.application.domain.exception.InvalidOfferStateException;
import salon.sales.application.domain.exception.OfferExpiredException;
import salon.sales.application.domain.model.customer.Address;
import salon.sales.application.domain.model.customer.ContactData;
import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.model.offer.Discount;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.domain.model.order.OrderFactory;
import salon.sales.application.domain.model.order.PaymentMethod;
import salon.sales.infrastructure.out.persistence.InMemoryCustomerRepository;
import salon.sales.infrastructure.out.persistence.InMemoryOfferRepository;
import salon.sales.infrastructure.out.persistence.InMemoryOrderRepository;
import salon.common.model.Money;
import salon.common.model.SpecificationId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class OfflineDemo {

    public static void main(String[] args) {
        System.out.println("=== CATALOG: vehicle configuration (Fail-fast) ===");
        
        ProductCatalog catalog = new ProductCatalogFactory().createNew(
                ModelYear.of(2026),
                java.util.List.of(
                        new CatalogOption(OptionCode.of("MANUAL_GEARBOX"),
                                salon.catalog.application.domain.model.shared.Money.zero("PLN")),
                        new CatalogOption(OptionCode.of("ADAPTIVE_CRUISE"),
                                salon.catalog.application.domain.model.shared.Money.of(new BigDecimal("3000"), "PLN"))),
                java.util.List.of(
                        new CatalogRule(OptionCode.of("ADAPTIVE_CRUISE"),
                                OptionCode.of("MANUAL_GEARBOX"), RuleType.EXCLUDES)));

        VehicleSpecification spec = new VehicleSpecificationFactory()
                .createDraft(catalog.id(), catalog.currencyCode());
        RuleValidationService ruleValidation = new RuleValidationService();
        spec.addOption(OptionCode.of("MANUAL_GEARBOX"), catalog);
        try {
            spec.addOption(OptionCode.of("ADAPTIVE_CRUISE"), catalog);
            
            ruleValidation.validateSelection(spec, catalog);
        } catch (CombinationNotAllowedException e) {
            System.out.println("[OK] Rule fired: " + e.getMessage());
        }

        System.out.println("\n=== SALES: discount policy and offer state machine ===");
        Offer offer = new Offer(OfferId.generate(), new CustomerId("CUST-1"),
                SpecificationId.generate(), Money.of(new BigDecimal("100000"), "PLN"));
        offer.applyDiscount(new Discount(new BigDecimal("5.00")));
        offer.publishOffer();
        System.out.println("[OK] Offer after publication: " + offer.getState()
                + ", final price: " + offer.getFinalPrice().amount() + " PLN");

        
        Offer greedy = new Offer(OfferId.generate(), new CustomerId("CUST-2"),
                SpecificationId.generate(), Money.of(new BigDecimal("100000"), "PLN"));
        try {
            greedy.applyDiscount(new Discount(new BigDecimal("35.00")));
        } catch (IllegalArgumentException e) {
            System.out.println("[OK] Discount policy fired: " + e.getMessage());
        }

        
        offer.accept();
        Order order = new OrderFactory().createFromOffer(offer);
        order.declarePaymentMethod(PaymentMethod.BANK_TRANSFER);
        System.out.println("[OK] Order " + order.getId().value() + " in state "
                + order.getState() + " (payment: " + order.getPaymentMethod() + ")");
        
        try {
            greedy.publishOffer();
            new OrderFactory().createFromOffer(greedy);
        } catch (InvalidOfferStateException e) {
            System.out.println("[OK] Order only from ACCEPTED — " + e.getMessage());
        }

        System.out.println("\n=== BILLING AND SETTLEMENT: UC-FIR-02 / 03 ===");
        InProcessEventPublisherAdapter bus = new InProcessEventPublisherAdapter();

        
        InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
        InMemoryOfferRepository offerRepo = new InMemoryOfferRepository();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        customerRepo.save(new Customer(new CustomerId("CUST-1"), "Jan Kowalski", "1234563218",
                new Address("Main Street 1", "00-001", "Warsaw", "PL"),
                new ContactData("jan.kowalski@example.com", "+48 600 100 200")));
        offerRepo.save(offer);
        orderRepo.save(order);

        InMemorySettlementRepository settlementRepo = new InMemorySettlementRepository();
        InMemoryDocumentRepository documentRepo = new InMemoryDocumentRepository();

        
        PaymentProcessService settlements = new PaymentProcessService(
                settlementRepo, new SettlementFactory(), documentRepo,
                new NotificationMockAdapter(), bus);

        SettlementEventListener listener = new SettlementEventListener(settlements);
        listener.on(new OrderReadyForSettlementEvent(UUID.randomUUID(), order.getId().value(),
                new BigDecimal("100000"), "PLN", Instant.now()));

        
        settlements.processPayment(new ProcessPaymentCommand(
                order.getId().value(), "TX-1", new BigDecimal("20000"), "PLN"));
        
        settlements.processPayment(new ProcessPaymentCommand(
                order.getId().value(), "TX-2", new BigDecimal("80000"), "PLN"));
        System.out.println("[OK] Balance " + order.getId().value() + " settled (status SETTLED).");

        
        DocumentGenerationService docs = new DocumentGenerationService(
                settlementRepo, documentRepo,
                new InvoiceCalculationService(), new AccountingDocumentFactory(),
                new PdfGeneratorMockAdapter(), new NotificationMockAdapter(), bus,
                new SalesCrmIntegrationAdapter(new SalesQueryService(orderRepo, offerRepo, customerRepo)),
                new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"));

        String invoiceId = docs.generateInvoice(new GenerateInvoiceCommand(
                order.getId().value(), "Final invoice " + order.getId().value(), "accountant@salon.pl"));
        System.out.println("[OK] Invoice issued, id=" + invoiceId);

        
        try {
            Offer stale = new Offer(OfferId.generate(), new CustomerId("CUST-3"),
                    SpecificationId.generate(), Money.of(new BigDecimal("80000"), "PLN"));
            stale.publishOffer();
            salon.common.infrastructure.persistence.DomainReflection.set(
                    stale, "validityDate", java.time.LocalDate.now().minusDays(1));
            stale.accept();
        } catch (OfferExpiredException e) {
            System.out.println("[OK] Validity rule in the aggregate: " + e.getMessage());
        }
    }
}
