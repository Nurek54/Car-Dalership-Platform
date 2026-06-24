package system;

import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;
import salon.common.model.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Makieta całego systemu salonu spinająca wszystkie konteksty (Sprzedaż, Logistyka, Fakturowanie,
 * Finansowanie) — odpowiednik korzenia kompozycji z {@code salon.bootstrap} (jak OfflineDemo), ale
 * na potrzeby testów systemowych.
 *
 * Wszystkie konteksty publikują na jeden, wspólny {@link RecordingEventPublisher}, dzięki czemu
 * test może asercją sprawdzić emisję zdarzeń. Integracje międzykontekstowe (ACL do Sprzedaży dla
 * Fakturowania i Finansowania) działają NAPRAWDĘ — przez realną fasadę zapytań {@code SalesQueryFacade}.
 * Routing zdarzeń pomiędzy kontekstami (rolę magistrali) realizują wysokopoziomowe metody poniżej,
 * które tłumaczą zdarzenia dziedzinowe na lokalne rekordy adapterów nasłuchujących.
 */
final class SalonSystem {

    /** Wspólny, nasłuchujący publisher zdarzeń — zbiera wszystko, co emitują konteksty. */
    static final class RecordingEventPublisher implements EventPublisher {
        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }

        boolean has(Class<? extends DomainEvent> type) {
            return events.stream().anyMatch(type::isInstance);
        }
    }

    final RecordingEventPublisher events = new RecordingEventPublisher();

    // --- Sprzedaż i CRM ---
    private final salon.sales.infrastructure.out.persistence.InMemoryCustomerRepository salesCustomerRepo =
            new salon.sales.infrastructure.out.persistence.InMemoryCustomerRepository();
    private final salon.sales.infrastructure.out.persistence.InMemoryOfferRepository salesOfferRepo =
            new salon.sales.infrastructure.out.persistence.InMemoryOfferRepository();
    private final salon.sales.infrastructure.out.persistence.InMemoryOrderRepository salesOrderRepo =
            new salon.sales.infrastructure.out.persistence.InMemoryOrderRepository();
    final salon.sales.api.SalesQueryFacade salesQuery =
            new salon.sales.application.service.SalesQueryService(salesOrderRepo, salesOfferRepo, salesCustomerRepo);
    private final salon.sales.application.service.SalesService sales =
            new salon.sales.application.service.SalesService(salesCustomerRepo, salesOfferRepo, salesOrderRepo,
                    new salon.sales.infrastructure.out.integration.BillingIntegrationAdapter(),
                    new salon.sales.infrastructure.out.integration.InventoryIntegrationAdapter(),
                    events,
                    new salon.sales.application.domain.model.offer.OfferFactory(),
                    new salon.sales.application.domain.model.order.OrderFactory());
    private final salon.sales.infrastructure.in.messaging.InventoryEventSubscriberAdapter salesInventorySub =
            new salon.sales.infrastructure.in.messaging.InventoryEventSubscriberAdapter(sales);

    // --- Inwentarz i Logistyka ---
    private final salon.logistics.infrastructure.out.mock.InMemoryInventoryRepository vehRepo =
            new salon.logistics.infrastructure.out.mock.InMemoryInventoryRepository();
    private final salon.logistics.infrastructure.out.mock.InMemorySpecificationReadModelAdapter logCatalog =
            new salon.logistics.infrastructure.out.mock.InMemorySpecificationReadModelAdapter();
    private final salon.logistics.application.service.InventoryManagementService logistics =
            new salon.logistics.application.service.InventoryManagementService(
                    vehRepo, logCatalog, (orderId, codes) -> "VIN-FAC-" + orderId, events);
    private final salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter logSalesSub =
            new salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter(logCatalog, logistics);
    private final salon.logistics.infrastructure.in.messaging.FinancingEventSubscriberAdapter logFinSub =
            new salon.logistics.infrastructure.in.messaging.FinancingEventSubscriberAdapter(logistics);
    private final salon.logistics.infrastructure.in.messaging.BillingEventSubscriberAdapter logBillSub =
            new salon.logistics.infrastructure.in.messaging.BillingEventSubscriberAdapter(logistics, logistics, logistics);
    private final salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory vehFactory =
            new salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory();

    // --- Fakturowanie i Rozliczenia ---
    private final salon.billing.infrastructure.out.mock.InMemorySettlementRepository settleRepo =
            new salon.billing.infrastructure.out.mock.InMemorySettlementRepository();
    private final salon.billing.infrastructure.out.mock.InMemoryDocumentRepository docRepo =
            new salon.billing.infrastructure.out.mock.InMemoryDocumentRepository();
    private final salon.billing.application.service.PaymentProcessService billingPayments =
            new salon.billing.application.service.PaymentProcessService(settleRepo,
                    new salon.billing.application.domain.model.settlement.SettlementFactory(), docRepo,
                    new salon.billing.infrastructure.out.mock.NotificationMockAdapter(), events);
    private final salon.billing.application.service.DocumentGenerationService billingDocs =
            new salon.billing.application.service.DocumentGenerationService(settleRepo, docRepo,
                    new salon.billing.application.domain.service.InvoiceCalculationService(),
                    new salon.billing.application.domain.model.document.AccountingDocumentFactory(),
                    new salon.billing.infrastructure.out.mock.PdfGeneratorMockAdapter(),
                    new salon.billing.infrastructure.out.mock.NotificationMockAdapter(), events,
                    new salon.billing.infrastructure.out.integration.SalesCrmIntegrationAdapter(salesQuery),
                    new salon.billing.application.domain.model.document.SellerDetails("Salon Sp. z o.o.", "5260000000"));
    private final salon.billing.infrastructure.in.messaging.BillingEventSubscriberAdapter billBillSub =
            new salon.billing.infrastructure.in.messaging.BillingEventSubscriberAdapter(billingDocs, billingDocs, "FA-Księgowy");

    // --- Finansowanie ---
    private final salon.financing.infrastructure.out.mock.InMemoryFinancingRepository finRepo =
            new salon.financing.infrastructure.out.mock.InMemoryFinancingRepository();
    private final salon.financing.application.service.ProcessFinancingService financing =
            new salon.financing.application.service.ProcessFinancingService(finRepo,
                    new salon.financing.application.domain.model.financing.FinancingApplicationFactory(),
                    new salon.financing.infrastructure.out.integration.SalesCrmIntegrationAdapter(salesQuery),
                    new salon.financing.infrastructure.out.mock.BankIntegrationMockAdapter(), events);
    private final salon.financing.infrastructure.in.messaging.FinancingEventListener finListener =
            new salon.financing.infrastructure.in.messaging.FinancingEventListener(financing);

    // ===================================================================================
    // Sprzedaż — wejścia
    // ===================================================================================
    void registerCustomer(String customerId, String name, String nip) {
        sales.registerCustomer(new salon.sales.application.domain.model.customer.Customer(
                new salon.sales.application.domain.model.customer.CustomerId(customerId), name, nip,
                new salon.sales.application.domain.model.customer.Address("Główna 1", "00-001", "Warszawa", "PL"),
                new salon.sales.application.domain.model.customer.ContactData(name.toLowerCase() + "@example.com", "+48600100200")));
    }

    String createOffer(String customerId, String specificationId, long price) {
        return sales.createProformaOffer(customerId, specificationId, Money.of(price, "PLN"));
    }

    String acceptOfferBankTransfer(String offerId) {
        return sales.acceptOffer(new salon.sales.application.command.AcceptOfferCommand(
                offerId, salon.sales.application.domain.model.order.PaymentMethod.BANK_TRANSFER));
    }

    String acceptOfferFinancing(String offerId) {
        return sales.acceptOffer(new salon.sales.application.command.AcceptOfferCommand(
                offerId, salon.sales.application.domain.model.order.PaymentMethod.FINANCING));
    }

    void salesScheduleAndRelease(String orderId) {
        sales.scheduleHandover(new salon.sales.application.command.ScheduleHandoverCommand(
                orderId, LocalDate.now().plusDays(3)));
        sales.releaseVehicle(orderId);
    }

    String salesOrderState(String orderId) {
        return salesOrderRepo.findById(new salon.common.model.OrderId(orderId))
                .map(o -> o.state().name()).orElse("NONE");
    }

    // ===================================================================================
    // Magistrala — routing zdarzeń pomiędzy kontekstami
    // ===================================================================================
    void seedStockVehicle(String vin, String specificationId) {
        vehRepo.save(vehFactory.createStockArrival(new salon.logistics.application.domain.model.vehicle.ImporterData(
                new salon.logistics.application.domain.model.vehicle.VinNumber(vin),
                new salon.logistics.application.domain.model.vehicle.SpecificationId(specificationId), java.util.List.of())));
    }

    void linkOrderToSpecification(String orderId, String specificationId) {
        logSalesSub.handleOrderPlaced(
                new salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter.OrderPlaced(orderId, specificationId));
    }

    void reserveOnBankTransfer(String orderId) {
        logFinSub.handleBankTransferDeclared(
                new salon.logistics.infrastructure.in.messaging.FinancingEventSubscriberAdapter.BankTransferDeclared(orderId));
    }

    void reserveOnFinancingApproved(String orderId) {
        logFinSub.handleFinancingApproved(
                new salon.logistics.infrastructure.in.messaging.FinancingEventSubscriberAdapter.FinancingApproved(orderId));
    }

    void factoryOrderOnAdvanceRegistered(String orderId) {
        logBillSub.handleAdvancePaymentRegistered(
                new salon.logistics.infrastructure.in.messaging.BillingEventSubscriberAdapter.AdvancePaymentRegistered(orderId));
    }

    void prepareForHandoverOnSettlement(String orderId) {
        logBillSub.handleSettlementCompleted(
                new salon.logistics.infrastructure.in.messaging.BillingEventSubscriberAdapter.SettlementCompleted(orderId));
    }

    void receiveVehicleAtYard(String vin) {
        logistics.receiveVehicle(vin);
    }

    void logisticsReleaseOnSalesCommand(String orderId) {
        logSalesSub.handleReleaseVehicle(
                new salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter.ReleaseVehicleCommand(orderId));
    }

    String vehicleStateByOrder(String orderId) {
        return vehRepo.findByOrderId(new salon.common.model.OrderId(orderId))
                .map(v -> v.state().name()).orElse("NONE");
    }

    String vehicleStateByVin(String vin) {
        return vehRepo.findByVin(new salon.logistics.application.domain.model.vehicle.VinNumber(vin))
                .map(v -> v.state().name()).orElse("NONE");
    }

    // ===================================================================================
    // Fakturowanie — wejścia
    // ===================================================================================
    void initBillingSettlement(String orderId, long total) {
        billingPayments.initializeSettlement(new salon.common.model.OrderId(orderId), Money.of(total, "PLN"));
    }

    void billingAdvanceOnNotOnStock(String orderId) {
        billBillSub.handleVehicleIsNotOnStock(
                new salon.billing.infrastructure.in.messaging.BillingEventSubscriberAdapter.VehicleIsNotOnStock(orderId));
    }

    void billingInvoiceOnReserved(String orderId, String vin) {
        billBillSub.handleVehicleReservedFromStock(
                new salon.billing.infrastructure.in.messaging.BillingEventSubscriberAdapter.VehicleReservedFromStock(orderId, vin));
    }

    void payOrder(String orderId, long amount) {
        billingPayments.processPayment(new salon.billing.application.command.ProcessPaymentCommand(
                orderId, "TX-" + orderId, BigDecimal.valueOf(amount), "PLN"));
    }

    String settlementStatus(String orderId) {
        return settleRepo.findByOrderId(new salon.common.model.OrderId(orderId))
                .map(s -> s.status().name()).orElse("NONE");
    }

    int documentsFor(String orderId) {
        return docRepo.findByOrderId(new salon.common.model.OrderId(orderId)).size();
    }

    // ===================================================================================
    // Sprzedaż — gotowość do wydania (z Logistyki)
    // ===================================================================================
    void salesMarkReadyOnVehicleReady(String orderId) {
        salesInventorySub.handleVehicleReadyForHandover(
                new salon.sales.infrastructure.in.messaging.InventoryEventSubscriberAdapter.VehicleReadyForHandover(orderId));
    }

    // ===================================================================================
    // Finansowanie — wejścia
    // ===================================================================================
    void requestFinancing(String orderId, String customerId) {
        finListener.handleFinancingRequested(
                new salon.financing.infrastructure.in.messaging.FinancingEventListener.FinancingRequested(orderId, customerId));
    }

    void bankDecision(String orderId, boolean approved) {
        finListener.handleFinancingDecisionReceived(
                new salon.financing.infrastructure.in.messaging.FinancingEventListener.FinancingDecisionReceivedFromBank(orderId, approved));
    }

    String financingState(String orderId) {
        return finRepo.findByOrderId(new salon.financing.application.domain.model.financing.OrderId(orderId))
                .map(a -> a.state().name()).orElse("NONE");
    }
}
