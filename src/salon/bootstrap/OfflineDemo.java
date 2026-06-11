package salon.bootstrap;

import salon.billing.application.command.GenerateInvoiceCommand;
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
import salon.billing.infrastructure.mock.InProcessEventPublisherAdapter;
import salon.billing.infrastructure.mock.NotificationMockAdapter;
import salon.billing.infrastructure.mock.PdfGeneratorMockAdapter;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.CatalogRule;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.catalog.RuleType;
import salon.catalog.domain.model.specification.RuleViolationException;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.sales.application.service.SalesQueryService;
import salon.sales.domain.exception.InvalidOfferStateException;
import salon.sales.domain.exception.OfferExpiredException;
import salon.sales.domain.model.customer.Address;
import salon.sales.domain.model.customer.ContactData;
import salon.sales.domain.model.customer.Customer;
import salon.sales.domain.model.customer.CustomerId;
import salon.sales.domain.model.offer.Discount;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.order.Order;
import salon.sales.domain.model.order.OrderFactory;
import salon.sales.domain.model.order.PaymentMethod;
import salon.sales.infrastructure.mock.InMemoryCustomerRepository;
import salon.sales.infrastructure.mock.InMemoryOfferRepository;
import salon.sales.infrastructure.mock.InMemoryOrderRepository;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Demo (offline): reguły domenowe poszczególnych agregatów w izolacji —
 * Katalog (Fail-fast EXCLUDES), Sprzedaż (polityka rabatowa + maszyna stanów oferty),
 * Fakturowanie i Rozliczenia (UC-FIR-02/03 — ścieżka "auto ze stocku").
 */
public class OfflineDemo {

    public static void main(String[] args) {
        System.out.println("=== KATALOG: konfiguracja pojazdu (Fail-fast) ===");
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(new CatalogOption(new OptionCode("MANUAL_GEARBOX"), Money.of(0, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("ADAPTIVE_CRUISE"), Money.of(3000, "PLN")));
        catalog.addRule(new CatalogRule(new OptionCode("ADAPTIVE_CRUISE"),
                new OptionCode("MANUAL_GEARBOX"), RuleType.EXCLUDES));
        VehicleSpecification spec = new VehicleSpecification(SpecificationId.generate(), catalog.getCatalogId());
        spec.addOption(new OptionCode("MANUAL_GEARBOX"), catalog);
        try {
            spec.addOption(new OptionCode("ADAPTIVE_CRUISE"), catalog);
        } catch (RuleViolationException e) {
            System.out.println("[OK] Reguła zadziałała: " + e.getMessage());
        }

        System.out.println("\n=== SPRZEDAŻ: polityka rabatowa i maszyna stanów oferty ===");
        Offer offer = new Offer(OfferId.generate(), new CustomerId("CUST-1"),
                SpecificationId.generate(), Money.of(new BigDecimal("100000"), "PLN"));
        offer.applyDiscount(new Discount(new BigDecimal("5.00")));
        offer.publishOffer();
        System.out.println("[OK] Oferta po publikacji: " + offer.getState()
                + ", cena końcowa: " + offer.getFinalPrice().amount() + " PLN");

        // Rabat ponad politykę salonu -> agregat odrzuca (hermetyzacja decyzji cenowych).
        Offer greedy = new Offer(OfferId.generate(), new CustomerId("CUST-2"),
                SpecificationId.generate(), Money.of(new BigDecimal("100000"), "PLN"));
        try {
            greedy.applyDiscount(new Discount(new BigDecimal("35.00")));
        } catch (IllegalArgumentException e) {
            System.out.println("[OK] Polityka rabatowa zadziałała: " + e.getMessage());
        }

        // Konwersja: zamówienie może powstać wyłącznie z oferty ACCEPTED (reguła w agregacie).
        offer.accept();
        Order order = new OrderFactory().createFromOffer(offer.getId(), offer.toSnapshot());
        order.declarePaymentMethod(PaymentMethod.BANK_TRANSFER);
        System.out.println("[OK] Zamówienie " + order.getId().value() + " w stanie "
                + order.getState() + " (płatność: " + order.getPaymentMethod() + ")");
        // Migawkę (toSnapshot) można zbudować WYŁĄCZNIE z oferty ACCEPTED — reguła w agregacie.
        try {
            greedy.publishOffer();
            new OrderFactory().createFromOffer(greedy.getId(), greedy.toSnapshot());
        } catch (InvalidOfferStateException e) {
            System.out.println("[OK] Migawka tylko z ACCEPTED — " + e.getMessage());
        }

        System.out.println("\n=== FAKTUROWANIE I ROZLICZENIA: UC-FIR-02 / 03 ===");
        InProcessEventPublisherAdapter bus = new InProcessEventPublisherAdapter();

        // Repozytoria CRM potrzebne adapterowi CrmIntegrationPort (dane nabywcy po orderId).
        InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
        InMemoryOfferRepository offerRepo = new InMemoryOfferRepository();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        customerRepo.save(new Customer(new CustomerId("CUST-1"), "Jan Kowalski", "1234563218",
                new Address("Marszałkowska 1", "00-001", "Warszawa", "PL"),
                new ContactData("jan.kowalski@example.com", "+48 600 100 200")));
        offerRepo.save(offer);
        orderRepo.save(order);

        InMemorySettlementRepository settlementRepo = new InMemorySettlementRepository();
        InMemoryDocumentRepository documentRepo = new InMemoryDocumentRepository();

        // --- UC-FIR-03: inicjalizacja salda + rejestracja wpłat ---
        SettlementAppService settlements = new SettlementAppService(
                settlementRepo, new SettlementFactory(), documentRepo,
                new NotificationMockAdapter(), bus);

        SettlementEventListener listener = new SettlementEventListener(settlements);
        listener.on(new OrderReadyForSettlementEvent(UUID.randomUUID(), order.getId().value(),
                new BigDecimal("100000"), "PLN", Instant.now()));

        // Wpłata częściowa -> PARTIAL_PAYMENT.
        settlements.processPayment(new ProcessPaymentCommand(
                order.getId().value(), "TX-1", new BigDecimal("20000"), "PLN"));
        // Dopłata do pełnej kwoty -> SETTLED + SettlementCompletedEvent.
        settlements.processPayment(new ProcessPaymentCommand(
                order.getId().value(), "TX-2", new BigDecimal("80000"), "PLN"));
        System.out.println("[OK] Saldo " + order.getId().value() + " rozliczone (status SETTLED).");

        // --- UC-FIR-02: faktura końcowa (dane nabywcy dociągnięte z kontekstu Sprzedaży) ---
        DocumentAppService docs = new DocumentAppService(
                settlementRepo, documentRepo,
                new InvoiceCalculationDomainService(), new AccountingDocumentFactory(),
                new PdfGeneratorMockAdapter(), new NotificationMockAdapter(), bus,
                new SalesCrmIntegrationAdapter(new SalesQueryService(orderRepo, offerRepo, customerRepo)),
                new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"));

        String invoiceId = docs.generateInvoice(new GenerateInvoiceCommand(
                order.getId().value(), "Faktura koncowa " + order.getId().value(), "ksiegowy@salon.pl"));
        System.out.println("[OK] Faktura wystawiona, id=" + invoiceId);

        // Reguła ważności w agregacie: oferta po terminie nie przejdzie accept().
        try {
            Offer stale = new Offer(OfferId.generate(), new CustomerId("CUST-3"), SpecificationId.generate());
            stale.publishOffer();
            salon.shared.infrastructure.persistence.DomainReflection.set(
                    stale, "validityDate", java.time.LocalDate.now().minusDays(1));
            stale.accept();
        } catch (OfferExpiredException e) {
            System.out.println("[OK] Reguła ważności w agregacie: " + e.getMessage());
        }
    }
}
