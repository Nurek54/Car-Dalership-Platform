package salon.bootstrap;

import salon.billing.application.command.GenerateAdvanceCommand;
import salon.billing.application.command.ProcessPaymentCommand;
import salon.billing.application.service.DocumentAppService;
import salon.billing.application.service.SettlementAppService;
import salon.billing.domain.event.AdvancePaymentRegisteredEvent;
import salon.billing.domain.event.AdvancePaymentRequestedEvent;
import salon.billing.domain.event.PaymentRegisteredEvent;
import salon.billing.domain.event.SettlementCompletedEvent;
import salon.billing.domain.model.document.AccountingDocumentFactory;
import salon.billing.domain.model.document.SellerDetails;
import salon.billing.domain.model.settlement.SettlementFactory;
import salon.billing.domain.service.InvoiceCalculationDomainService;
import salon.billing.infrastructure.integration.SalesCrmIntegrationAdapter;
import salon.billing.infrastructure.mock.InMemoryDocumentRepository;
import salon.billing.infrastructure.mock.InMemorySettlementRepository;
import salon.billing.infrastructure.mock.NotificationMockAdapter;
import salon.billing.infrastructure.mock.PdfGeneratorMockAdapter;
import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.logistics.application.InventoryManagementAppService;
import salon.logistics.domain.event.FactoryOrderPlacedEvent;
import salon.logistics.domain.event.VehicleDeliveredToStockEvent;
import salon.logistics.domain.event.VehicleInventoryReleasedEvent;
import salon.logistics.domain.event.VehicleIsNotOnStockEvent;
import salon.logistics.domain.event.VehicleReadyForHandoverEvent;
import salon.logistics.infrastructure.mock.FactoryIntegrationMockAdapter;
import salon.logistics.infrastructure.mock.InMemoryInventoryRepository;
import salon.logistics.infrastructure.mock.SpecificationIntegrationMockAdapter;
import salon.sales.application.command.ScheduleHandoverCommand;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.service.SalesAppService;
import salon.sales.domain.event.OrderPlacedEvent;
import salon.sales.domain.model.customer.Address;
import salon.sales.domain.model.customer.ContactData;
import salon.sales.domain.model.customer.Customer;
import salon.sales.domain.model.customer.CustomerId;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.infrastructure.integration.InventoryCommandAdapter;
import salon.sales.infrastructure.mock.InMemoryCustomerRepository;
import salon.sales.infrastructure.mock.InMemoryOfferRepository;
import salon.sales.infrastructure.mock.InMemoryOrderRepository;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Demo (offline, in-process): pełna choreografia "Long Track" zgodna z PDF —
 * UC-CRM-01..05 + UC-INW-01/02/03/05/06 + UC-FIR-01/03.
 *
 * Przebieg: konfigurator -> oferta proforma (wycena z Katalogu) -> akceptacja
 * -> brak auta na placu -> prośba o zadatek -> wpłata zadatku (aktywacja zamówienia
 * + zlecenie produkcji) -> dostawa na plac -> dopłata salda (SettlementCompleted
 * -> gotowy do wydania) -> umówienie odbioru -> wydanie pojazdu (ReleaseVehicle).
 */
public class SalonDemo {

    public static void main(String[] args) {
        InProcessChoreographyBus bus = new InProcessChoreographyBus();

        // --- INWENTARZ I LOGISTYKA ---
        InMemoryInventoryRepository inventoryRepo = new InMemoryInventoryRepository();
        InventoryManagementAppService inventory = new InventoryManagementAppService(
                inventoryRepo, new SpecificationIntegrationMockAdapter(),
                new FactoryIntegrationMockAdapter(), bus);

        // --- SPRZEDAŻ I CRM ---
        InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
        InMemoryOfferRepository offerRepo = new InMemoryOfferRepository();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        SalesAppService sales = new SalesAppService(
                customerRepo, offerRepo, orderRepo, bus,
                new DemoCatalogPriceList(),                                  // wycena z Katalogu
                new InventoryCommandAdapter(inventory, inventory, inventoryRepo),
                null);                                                       // billing przez zdarzenia

        // --- FAKTUROWANIE I ROZLICZENIA ---
        InMemorySettlementRepository settlementRepo = new InMemorySettlementRepository();
        InMemoryDocumentRepository documentRepo = new InMemoryDocumentRepository();
        SettlementAppService settlements = new SettlementAppService(
                settlementRepo, new SettlementFactory(), documentRepo,
                new NotificationMockAdapter(), bus);
        DocumentAppService documents = new DocumentAppService(
                settlementRepo, documentRepo,
                new InvoiceCalculationDomainService(), new AccountingDocumentFactory(),
                new PdfGeneratorMockAdapter(), new NotificationMockAdapter(), bus,
                new SalesCrmIntegrationAdapter(orderRepo, offerRepo, customerRepo),
                new SellerDetails("Salon Samochodowy Sp. z o.o.", "5260000000"));

        // --- Okablowanie choreografii (subskrypcje jak na kanwach kontekstów) ---
        bus.sales = sales;
        bus.inventory = inventory;
        bus.documents = documents;
        bus.settlements = settlements;
        bus.orderRepo = orderRepo;

        // ===== Scenariusz =====
        sales.registerCustomer(new Customer(new CustomerId("CUST-1"), "Jan Kowalski", "1234563218",
                new Address("Marszałkowska 1", "00-001", "Warszawa", "PL"),
                new ContactData("jan.kowalski@example.com", "+48 600 100 200")));

        System.out.println("=== UC-CRM-01: uruchomienie sesji konfiguratora ===");
        String sessionId = sales.startConfiguratorSession(
                new StartConfiguratorSessionCommand("CUST-1", "SP-7"));
        System.out.println("Sesja: " + sessionId);

        System.out.println("\n=== UC-CRM-02: oferta proforma (wycena z Katalogu: 100 000 PLN) ===");
        OfferId offerId = sales.generateOffer("CUST-1", "SPEC-1");
        System.out.println("Oferta " + offerId.value() + ": "
                + offerRepo.findById(offerId).get().getState());

        System.out.println("\n=== UC-CRM-03: akceptacja oferty i utworzenie zamówienia ===");
        String orderId = sales.acceptOfferAndCreateOrder(offerId);
        System.out.println("Zamówienie " + orderId + ": "
                + orderRepo.findById(new OrderId(orderId)).get().getState());

        System.out.println("\n=== UC-FIR-03: wpłata zadatku (10% = 10 000 PLN) ===");
        settlements.processPayment(new ProcessPaymentCommand(
                orderId, "TX-1", new BigDecimal("10000.00"), "PLN"));
        System.out.println("Zamówienie po zadatku: "
                + orderRepo.findById(new OrderId(orderId)).get().getState());

        System.out.println("\n=== UC-INW-03: dostawa pojazdu z fabryki na plac ===");
        String vin = inventoryRepo.findByOrderId(new OrderId(orderId)).get().getVin().value();
        inventory.receiveVehicle(vin);

        System.out.println("\n=== UC-FIR-03: dopłata pozostałego salda ===");
        settlements.processPayment(new ProcessPaymentCommand(
                orderId, "TX-2", new BigDecimal("90000.00"), "PLN"));

        System.out.println("\n=== UC-CRM-04: umówienie odbioru ===");
        sales.scheduleHandover(new ScheduleHandoverCommand(orderId, LocalDate.now().plusDays(3)));
        System.out.println("Zamówienie: " + orderRepo.findById(new OrderId(orderId)).get().getState());

        System.out.println("\n=== UC-CRM-05: rejestracja fizycznego wydania pojazdu ===");
        sales.confirmHandover(new OrderId(orderId));
        System.out.println("Zamówienie: " + orderRepo.findById(new OrderId(orderId)).get().getState());
        System.out.println("Pojazd:     " + inventoryRepo.findByVin(
                inventoryRepo.findAll().get(0).getVin()).get().getState());

        System.out.println("\n[OK] Pełny cykl PDF (Long Track) zakończony.");
    }

    /** Cennik demo: każda specyfikacja wyceniana na 100 000 PLN (UC-CRM-02). */
    private static final class DemoCatalogPriceList implements CatalogRepository {
        @Override
        public Money getSpecificationPrice(String specificationId) {
            return Money.of(new BigDecimal("100000"), "PLN");
        }

        @Override
        public void save(ProductCatalog catalog) {
            throw new UnsupportedOperationException("Demo price list is read-only.");
        }

        @Override
        public Optional<ProductCatalog> findById(CatalogId id) {
            return Optional.empty();
        }

        @Override
        public List<ProductCatalog> findAll() {
            return List.of();
        }
    }

    /**
     * Magistrala in-process: rozsyła zdarzenia domenowe do subskrybentów innych kontekstów
     * dokładnie tak, jak na kanwach (Inbound/Outbound Communication).
     */
    private static final class InProcessChoreographyBus implements EventPublisherPort {

        SalesAppService sales;
        InventoryManagementAppService inventory;
        DocumentAppService documents;
        SettlementAppService settlements;
        InMemoryOrderRepository orderRepo;

        private final List<DomainEvent> pending = new ArrayList<>();
        private boolean dispatching = false;

        @Override
        public void publish(DomainEvent event) {
            System.out.println("   [bus] -> " + event.getClass().getSimpleName());
            this.pending.add(event);
            if (this.dispatching) {
                return; // zdarzenie obsłuży pętla nadrzędna (kolejność FIFO, bez rekurencji)
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
            // Sprzedaż -> Rozliczenia: nowe zamówienie -> inicjalizacja salda (wartość kontraktu).
            if (event instanceof OrderPlacedEvent e && settlements != null && orderRepo != null) {
                orderRepo.findById(new OrderId(e.orderId())).ifPresent(order ->
                        settlements.initializeSettlement(order.getId(), order.getRequiredDeposit()));
            }
            // Inwentarz -> Rozliczenia: brak auta -> prośba o zadatek (UC-FIR-01).
            if (event instanceof VehicleIsNotOnStockEvent e && documents != null) {
                documents.generateAdvance(new GenerateAdvanceCommand(e.orderId(), "ksiegowy@salon.pl"));
            }
            // Rozliczenia -> Sprzedaż: wpłata zaksięgowana -> aktywacja zamówienia (UC-CRM-03 cz.2).
            if (event instanceof PaymentRegisteredEvent e && sales != null) {
                sales.activateOnDeposit(e.orderId());
            }
            // Rozliczenia -> Inwentarz: zadatek zaksięgowany -> zlecenie produkcji (UC-INW-02).
            if (event instanceof AdvancePaymentRegisteredEvent e && inventory != null) {
                inventory.orderVehicleFromFactory(e.orderId());
            }
            // Rozliczenia -> Inwentarz: saldo = 0 -> przygotowanie do wydania (UC-INW-05).
            if (event instanceof SettlementCompletedEvent e && inventory != null) {
                inventory.prepareVehicleForHandover(e.orderId());
            }
            // Inwentarz -> Sprzedaż: pojazd gotowy -> "Gotowe do odbioru" + przypisanie VIN (UC-CRM-04).
            if (event instanceof VehicleReadyForHandoverEvent e && sales != null) {
                sales.markOrderAsReadyForHandover(new OrderId(e.orderId()));
                orderRepo.findById(new OrderId(e.orderId())).ifPresent(order -> {
                    order.assignVehicle(e.vin());
                    orderRepo.save(order);
                });
            }
            // Zdarzenia informacyjne:
            if (event instanceof AdvancePaymentRequestedEvent e) {
                System.out.println("   [CRM] Klient zamówienia " + e.orderId()
                        + " poproszony o zadatek (e-mail z danymi do przelewu).");
            }
            if (event instanceof FactoryOrderPlacedEvent e) {
                System.out.println("   [INW] Zlecenie produkcji przyjęte, VIN=" + e.vin() + ".");
            }
            if (event instanceof VehicleDeliveredToStockEvent e) {
                System.out.println("   [INW] Pojazd " + e.vin() + " dostarczony i sparowany z "
                        + e.orderId() + ".");
            }
            if (event instanceof VehicleInventoryReleasedEvent e) {
                System.out.println("   [INW] Pojazd " + e.vin() + " wyksięgowany (HANDED_OVER).");
            }
        }
    }
}
