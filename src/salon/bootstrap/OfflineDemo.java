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
import salon.sales.infrastructure.out.mock.InMemoryCustomerRepository;
import salon.sales.infrastructure.out.mock.InMemoryOfferRepository;
import salon.sales.infrastructure.out.mock.InMemoryOrderRepository;
import salon.common.model.Money;
import salon.common.model.SpecificationId;

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
        // Katalog jest niezmienny — budujemy go fabryką z kompletnego zbioru opcji i reguł.
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
            // Reguły wykluczające waliduje usługa dziedziny (a nie sam agregat).
            ruleValidation.validateSelection(spec, catalog);
        } catch (CombinationNotAllowedException e) {
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

        // Repozytoria CRM potrzebne adapterowi SalesIntegration (dane nabywcy po orderId).
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
        PaymentProcessService settlements = new PaymentProcessService(
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
        DocumentGenerationService docs = new DocumentGenerationService(
                settlementRepo, documentRepo,
                new InvoiceCalculationService(), new AccountingDocumentFactory(),
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
            salon.common.infrastructure.persistence.DomainReflection.set(
                    stale, "validityDate", java.time.LocalDate.now().minusDays(1));
            stale.accept();
        } catch (OfferExpiredException e) {
            System.out.println("[OK] Reguła ważności w agregacie: " + e.getMessage());
        }
    }
}
