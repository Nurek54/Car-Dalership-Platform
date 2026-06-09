package salon.sales.application.service;

import salon.sales.application.port.in.ActivateOrderOnDepositUseCase;
import salon.sales.application.port.in.CancelOrderCommand;
import salon.sales.application.port.in.CancelOrderUseCase;
import salon.sales.application.port.in.CreateOfferCommand;
import salon.sales.application.port.in.CreateOfferUseCase;
import salon.sales.application.port.in.CreateOrderCommand;
import salon.sales.application.port.out.CustomerRepository;
import salon.sales.application.port.out.FinancingIntegrationPort;
import salon.sales.application.port.out.InventoryIntegrationPort;
import salon.sales.domain.model.customer.Customer;
import salon.sales.domain.model.offer.OfferId;

/**
 * Orkiestrator Kontekstu Sprzedaży i CRM — węzeł "SalesAppService"
 * z docs/Architecture/SalesArchitecture.md.
 *
 * Spina porty wejściowe (UC-SPR-01..08) z fabrykami (OfferFactory/OrderFactory),
 * repozytoriami (Customer/Offer/Order) oraz portami integracyjnymi do Inwentarza
 * i Finansowania. Same reguły biznesowe pozostają w agregatach; tutaj jest tylko
 * koordynacja przypadków użycia.
 *
 * Logikę ofert i zamówień deleguje do {@link OfferAppService} i {@link OrderAppService}
 * (po nich wołają istniejące adaptery: REST, cron, subskrybenci zdarzeń). Porty
 * integracyjne mogą być null w konfiguracjach offline/demo.
 */
public class SalesAppService
        implements CreateOfferUseCase, CancelOrderUseCase, ActivateOrderOnDepositUseCase {

    private final OfferAppService offerAppService;
    private final OrderAppService orderAppService;
    private final CustomerRepository customerRepository;
    private final InventoryIntegrationPort inventoryPort;   // może być null (demo/offline)
    private final FinancingIntegrationPort financingPort;   // może być null (demo/offline)

    public SalesAppService(OfferAppService offerAppService,
                           OrderAppService orderAppService,
                           CustomerRepository customerRepository,
                           InventoryIntegrationPort inventoryPort,
                           FinancingIntegrationPort financingPort) {
        if (offerAppService == null) {
            throw new IllegalArgumentException("offerAppService must not be null.");
        }
        if (orderAppService == null) {
            throw new IllegalArgumentException("orderAppService must not be null.");
        }
        if (customerRepository == null) {
            throw new IllegalArgumentException("customerRepository must not be null.");
        }
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

    // --- UC-SPR-01: oferta/proforma ---

    @Override
    public OfferId createOffer(CreateOfferCommand command) {
        return offerAppService.createOffer(command);
    }

    // --- UC-SPR-02: konwersja oferty w zamówienie ---

    public String createOrderFromOffer(CreateOrderCommand command) {
        return orderAppService.createOrderFromOffer(command);
    }

    // --- UC-SPR-02 krok 5: aktywacja po zaksięgowaniu zadatku ---

    @Override
    public void activateOnDeposit(String orderId) {
        orderAppService.activateOnDeposit(orderId);
    }

    // --- UC-SPR-03: anulowanie zamówienia ---

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
