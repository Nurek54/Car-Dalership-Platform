package salon.bootstrap;

import salon.billing.application.port.in.GenerateInvoiceCommand;
import salon.billing.application.port.in.ProcessPaymentCommand;
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
import salon.billing.infrastructure.mock.InProcessEventPublisherAdapter;
import salon.billing.infrastructure.mock.NotificationMockAdapter;
import salon.billing.infrastructure.mock.PaymentGatewayMockAdapter;
import salon.billing.infrastructure.mock.PdfGeneratorMockAdapter;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.CatalogRule;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.catalog.RuleType;
import salon.catalog.domain.model.specification.RuleViolationException;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.sales.domain.model.customer.CustomerId;
import salon.sales.domain.model.offer.Discount;
import salon.sales.domain.model.offer.DiscountLimit;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Demo OFFLINE (bez brokera) — pokazuje, że domena wszystkich trzech kontekstów działa.
 * Uruchom: main(). Zdarzenia są tylko logowane (InProcessEventPublisherAdapter).
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

        System.out.println("\n=== SPRZEDAŻ: rabat ponad limit -> zgoda Dyrektora ===");
        Offer offer = new Offer(OfferId.generate(), new CustomerId("CUST-1"), SpecificationId.generate());
        offer.applyDiscount(new Discount(new BigDecimal("8.00")), new DiscountLimit(new BigDecimal("5.00")));
        System.out.println("[OK] Stan oferty: " + offer.getState());

        System.out.println("\n=== FAKTUROWANIE I ROZLICZENIA: UC-FIR-01 / 02 / 03 ===");
        InProcessEventPublisherAdapter bus = new InProcessEventPublisherAdapter();

        // Wspólne repozytorium salda (czytane przez oba serwisy).
        InMemorySettlementRepository settlementRepo = new InMemorySettlementRepository();

        // --- UC-FIR-03: inicjalizacja salda + rejestracja wpłat ---
        SettlementAppService settlements = new SettlementAppService(
                settlementRepo, new SettlementFactory(), bus, new PaymentGatewayMockAdapter());

        // Inicjalizacja przez zdarzenie "zamówienie gotowe do rozliczenia" (kontrakt 100 000 PLN).
        SettlementEventListener listener = new SettlementEventListener(settlements);
        listener.on(new OrderReadyForSettlementEvent(
                UUID.randomUUID(), "ORD-1", new BigDecimal("100000"), "PLN", Instant.now()));

        // Wpłata częściowa -> PARTIAL_PAYMENT.
        settlements.processPayment(new ProcessPaymentCommand(
                "ORD-1", "TX-1", new BigDecimal("20000"), "PLN", "GTW-1"));
        // Dopłata do pełnej kwoty -> SETTLED + SettlementCompletedEvent.
        settlements.processPayment(new ProcessPaymentCommand(
                "ORD-1", "TX-2", new BigDecimal("80000"), "PLN", null));
        System.out.println("[OK] Saldo ORD-1 rozliczone (status SETTLED).");

        // --- UC-FIR-02: faktura końcowa ---
        DocumentAppService docs = new DocumentAppService(
                settlementRepo, new InMemoryDocumentRepository(),
                new InvoiceCalculationDomainService(), new AccountingDocumentFactory(),
                new PdfGeneratorMockAdapter(), new NotificationMockAdapter(), bus,
                new CrmIntegrationMockAdapter(),
                new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"));

        String invoiceId = docs.generateInvoice(new GenerateInvoiceCommand(
                "ORD-1", "Faktura koncowa ORD-1", "ksiegowy@salon.pl"));
        System.out.println("[OK] Faktura wystawiona, id=" + invoiceId);
    }
}
