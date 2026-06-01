package salon.bootstrap;

import salon.billing.application.port.in.IssueDocumentCommand;
import salon.billing.application.port.in.RegisterPaymentCommand;
import salon.billing.application.service.DocumentAppService;
import salon.billing.application.service.PaymentAppService;
import salon.billing.application.service.SettlementAppService;
import salon.billing.domain.model.document.DocumentType;
import salon.billing.domain.service.PaymentClassificationService;
import salon.billing.domain.service.SettlementCalculationService;
import salon.billing.infrastructure.messaging.OrderReadyForSettlementEvent;
import salon.billing.infrastructure.messaging.SettlementEventListener;
import salon.billing.infrastructure.mock.ExternalIntegrationMockAdapter;
import salon.billing.infrastructure.mock.InMemoryDocumentRepository;
import salon.billing.infrastructure.mock.InMemoryPaymentRepository;
import salon.billing.infrastructure.mock.InMemorySettlementRepository;
import salon.billing.infrastructure.mock.InProcessEventPublisherAdapter;
import salon.billing.infrastructure.mock.KsefMockAdapter;
import salon.billing.infrastructure.mock.PaymentGatewayMockAdapter;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.CatalogRule;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.catalog.RuleType;
import salon.catalog.domain.model.specification.RuleViolationException;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.sales.domain.model.offer.CustomerId;
import salon.sales.domain.model.offer.Discount;
import salon.sales.domain.model.offer.DiscountLimit;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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

        System.out.println("\n=== ROZLICZENIA: UC-ROZ-01 / 02 / 03 ===");
        InProcessEventPublisherAdapter bus = new InProcessEventPublisherAdapter();
        PaymentAppService payments = new PaymentAppService(
                new InMemoryPaymentRepository(), bus,
                new PaymentGatewayMockAdapter(), new PaymentClassificationService());
        payments.registerPayment(new RegisterPaymentCommand(
                "ORD-1", new BigDecimal("20000"), "PLN", new BigDecimal("100000"), "TX-1"));

        DocumentAppService docs = new DocumentAppService(
                new InMemoryDocumentRepository(), new KsefMockAdapter(), bus);
        String docId = docs.issueDocument(new IssueDocumentCommand(
                DocumentType.VAT_INVOICE, "Jan Kowalski", "1234567890",
                List.of(new IssueDocumentCommand.LineData("Wymiana oleju", new BigDecimal("300"), "PLN"))));
        System.out.println("[OK] Faktura wystawiona, id=" + docId);

        SettlementAppService settlements = new SettlementAppService(
                new InMemorySettlementRepository(), new ExternalIntegrationMockAdapter(),
                new SettlementCalculationService());
        SettlementEventListener listener = new SettlementEventListener(settlements);
        listener.on(new OrderReadyForSettlementEvent(UUID.randomUUID(), "ORD-1",
                new BigDecimal("100000"), new BigDecimal("20000"), "PLN", Instant.now()));
        System.out.println("[OK] Rozliczenie policzone (saldo 30000, SETTLED).");
    }
}
