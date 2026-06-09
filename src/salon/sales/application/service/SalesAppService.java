package salon.sales.application.service;

import salon.sales.application.port.in.ActivateOrderOnDepositUseCase;
import salon.sales.application.port.in.CancelOrderCommand;
import salon.sales.application.port.in.CancelOrderUseCase;
import salon.sales.application.port.in.CreateOfferCommand;
import salon.sales.application.port.in.CreateOfferUseCase;
import salon.sales.application.port.in.CompleteHandoverUseCase;
import salon.sales.application.port.in.CreateOrderCommand;
import salon.sales.application.port.in.ScheduleHandoverCommand;
import salon.sales.application.port.in.ScheduleHandoverUseCase;
import salon.sales.application.port.in.StartConfiguratorSessionCommand;
import salon.sales.application.port.in.StartConfiguratorSessionUseCase;
import salon.sales.application.port.out.CustomerRepository;
import salon.sales.application.port.out.FinancingIntegrationPort;
import salon.sales.application.port.out.InventoryIntegrationPort;
import salon.sales.domain.model.customer.Customer;
import salon.sales.domain.model.offer.OfferId;

/**
 * Orkiestrator Kontekstu Sprzedaży i CRM — węzeł "SalesAppService"
 * z docs/Architecture/SalesArchitecture.md.
 *
 * Spina porty wejściowe (UC-CRM-01..05 / UC-SPR-01..08) z fabrykami (OfferFactory/OrderFactory),
 * repozytoriami (Customer/Offer/Order) oraz portami integracyjnymi do Inwentarza i Finansowania.
 * Reguły biznesowe pozostają w agregatach; tutaj jest tylko koordynacja przypadków użycia.
 *
 * Logikę deleguje do wyspecjalizowanych usług: {@link ConfiguratorAppService} (UC-CRM-01),
 * {@link OfferAppService} (UC-CRM-02) i {@link OrderAppService} (UC-CRM-03..05), po których
 * wołają adaptery (REST, cron, subskrybenci zdarzeń). Porty integracyjne mogą być null w demo.
 */
public class SalesAppService
        implements CreateOfferUseCase, CancelOrderUseCase, ActivateOrderOnDepositUseCase,
        StartConfiguratorSessionUseCase, ScheduleHandoverUseCase, CompleteHandoverUseCase {

    private final ConfiguratorAppService configuratorAppService;
    private final OfferAppService offerAppService;
    private final OrderAppService orderAppService;
    private final CustomerRepository customerRepository;
    private final InventoryIntegrationPort inventoryPort;   // może być null (demo/offline)
    private final FinancingIntegrationPort financingPort;   // może być null (demo/offline)

    public SalesAppService(ConfiguratorAppService configuratorAppService,
                           OfferAppService offerAppService,
                           OrderAppService orderAppService,
                           CustomerRepository customerRepository,
                           InventoryIntegrationPort inventoryPort,
                           FinancingIntegrationPort financingPort) {
        if (configuratorAppService == null) {
            throw new IllegalArgumentException("configuratorAppService must not be null.");
        }
        if (offerAppService == null) {
            throw new IllegalArgumentException("offerAppService must not be null.");
        }
        if (orderAppService == null) {
            throw new IllegalArgumentException("orderAppService must not be null.");
        }
        if (customerRepository == null) {
            throw new IllegalArgumentException("customerRepository must not be null.");
        }
        this.configuratorAppService = configuratorAppService;
        this.offerAppService = offerAppService;
        this.orderAppService = orderAppService;
        this.customerRepository = customerRepository;
        this.inventoryPort = inventoryPort;
        this.financingPort = financingPort;
    }

    // --- Klient (CRM) ---

    /** Rejestracja/zapis klienta w kontekście Sprzedaży. */
    public void registerCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null.");
        }
        customerRepository.save(customer);
    }

    // --- UC-CRM-01: uruchomienie sesji konfiguratora ---

    @Override
    public String startConfiguratorSession(StartConfiguratorSessionCommand command) {
        return configuratorAppService.startConfiguratorSession(command);
    }

    // --- UC-CRM-02: oferta/proforma ---

    @Override
    public OfferId createOffer(CreateOfferCommand command) {
        return offerAppService.createOffer(command);
    }

    // --- UC-CRM-03: konwersja oferty w zamówienie ---

    public String createOrderFromOffer(CreateOrderCommand command) {
        return orderAppService.createOrderFromOffer(command);
    }

    // --- UC-CRM-03 (krok 5): aktywacja po zaksięgowaniu zadatku ---

    @Override
    public void activateOnDeposit(String orderId) {
        orderAppService.activateOnDeposit(orderId);
    }

    // --- UC-CRM-04: gotowość pojazdu i umówienie odbioru ---

    /** Reakcja na gotowość pojazdu (sygnał z Inwentarza) — stan READY_FOR_HANDOVER. */
    public void markReadyForHandover(String orderId) {
        orderAppService.markReadyForHandover(orderId);
    }

    @Override
    public void scheduleHandover(ScheduleHandoverCommand command) {
        orderAppService.scheduleHandover(command);
    }

    // --- UC-CRM-05: rejestracja fizycznego wydania pojazdu ---

    @Override
    public void completeHandover(String orderId) {
        orderAppService.completeHandover(orderId);
    }

    // --- UC-CRM-03 (alt. A): anulowanie zamówienia ---

    @Override
    public void cancelOrder(CancelOrderCommand command) {
        orderAppService.cancelOrder(command);
    }

    // Dostęp do współpracowników (dla wiring/adapterów), gdyby był potrzebny.
    public OfferAppService offers() {
        return this.offerAppService;
    }

    public OrderAppService orders() {
        return this.orderAppService;
    }

    public InventoryIntegrationPort inventory() {
        return this.inventoryPort;
    }

    public FinancingIntegrationPort financing() {
        return this.financingPort;
    }
}
