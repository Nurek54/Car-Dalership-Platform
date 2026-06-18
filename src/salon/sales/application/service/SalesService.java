package salon.sales.application.service;

import org.springframework.stereotype.Service;
import salon.sales.application.port.in.AcceptOffer;
import salon.sales.application.port.in.ActivateOrderOnDeposit;
import salon.sales.application.port.in.ReceiveSpecificationUseCase;
import salon.sales.application.port.in.ReleaseVehicle;
import salon.sales.application.command.ScheduleHandoverCommand;
import salon.sales.application.port.in.ScheduleHandover;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.port.in.StartConfigurator;
import salon.sales.application.port.in.SynchronizeSpecificationPriceUseCase;
import salon.sales.application.port.out.BillingIntegration;
import salon.sales.application.port.out.CustomerDatabaseRepository;
import salon.sales.application.port.out.InventoryIntegration;
import salon.sales.application.port.out.OfferDatabaseRepository;
import salon.sales.application.port.out.OrderDatabaseRepository;
import salon.sales.application.port.out.SpecificationPriceReadModelPort;
import salon.sales.application.domain.event.ConfiguratorSessionInitiatedEvent;
import salon.sales.application.domain.exception.OfferNotFoundException;
import salon.sales.application.domain.exception.OrderNotFoundException;
import salon.sales.application.domain.exception.SpecificationNotFoundException;
import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferFactory;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.offer.OfferState;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.domain.model.order.OrderFactory;
import salon.sales.application.domain.model.order.OrderState;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.common.model.SpecificationId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Scentralizowana usługa aplikacyjna Kontekstu Sprzedaży i CRM — węzeł "SalesService"
 * z docs/Architecture/SalesArchitecture.md (PDF rozdz. 3.3.3, Rys. 23).
 *
 * Obsługuje przypadki użycia UC-CRM-01..05:
 *   startConfiguratorSession (UC-CRM-01, bezstanowy wyzwalacz),
 *   generateOffer            (UC-CRM-02, cena z lokalnego read modelu — event-carried),
 *   acceptOfferAndCreateOrder(UC-CRM-03, reguły ważności/konwersji w agregacie Offer),
 *   activateOnDeposit        (UC-CRM-03 cz.2 — aktywacja po zaksięgowanej wpłacie),
 *   markOrderAsReadyForHandover + scheduleHandover (UC-CRM-04),
 *   confirmHandover          (UC-CRM-05, komenda ReleaseVehicle przez InventoryIntegration),
 *   cancelOrder, expireOutdatedOffers, revertHandoverOnInventoryError (kompensata A1).
 *
 * Reguły biznesowe pozostają w agregatach (Offer/Order) i fabrykach (OfferFactory/
 * OrderFactory); tutaj jest wyłącznie orkiestracja: pobranie agregatu, wywołanie metody,
 * zapis i publikacja zdarzeń WYGENEROWANYCH PRZEZ AGREGAT (publishAll po zapisie).
 * Porty integracyjne mogą być null w uruchomieniach częściowych (testy jednostkowe).
 */
@Service
public class SalesService
        implements StartConfigurator, ReceiveSpecificationUseCase, AcceptOffer,
        ActivateOrderOnDeposit, ScheduleHandover, ReleaseVehicle,
        SynchronizeSpecificationPriceUseCase {

    private final CustomerDatabaseRepository customerRepository;
    private final OfferDatabaseRepository offerRepository;
    private final OrderDatabaseRepository orderRepository;
    private final EventPublisher eventPublisher;
    private final SpecificationPriceReadModelPort specificationPriceReadModel;
    private final InventoryIntegration inventoryPort;
    private final BillingIntegration billingPort;

    private final OfferFactory offerFactory = new OfferFactory();
    private final OrderFactory orderFactory = new OrderFactory();

    public SalesService(CustomerDatabaseRepository customerRepository,
                           OfferDatabaseRepository offerRepository,
                           OrderDatabaseRepository orderRepository,
                           EventPublisher eventPublisher,
                           SpecificationPriceReadModelPort specificationPriceReadModel,
                           InventoryIntegration inventoryPort,
                           BillingIntegration billingPort) {
        this.customerRepository = customerRepository;
        this.offerRepository = offerRepository;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.specificationPriceReadModel = specificationPriceReadModel;
        this.inventoryPort = inventoryPort;
        this.billingPort = billingPort;
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

    /**
     * Bezstanowy wyzwalacz (PDF rozdz. 3.3.3): nie tworzy agregatów — generuje identyfikator
     * sesji i emituje zdarzenie InitiateConfiguratorSession, na które reaguje Kontekst Katalogu.
     */
    @Override
    public String startConfiguratorSession(StartConfiguratorSessionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        String sessionId = "CFG-" + UUID.randomUUID();
        eventPublisher.publish(new ConfiguratorSessionInitiatedEvent(
                UUID.randomUUID(),
                sessionId,
                command.customerId(),
                command.salespersonId(),
                Instant.now()));
        return sessionId;
    }

    // --- UC-CRM-02: wygenerowanie oferty proforma ---

    /**
     * Wycena specyfikacji pochodzi z lokalnego read modelu Sprzedaży (zasilonego zdarzeniem
     * SpecificationCompleted z Katalogu) — bez synchronicznego odpytywania innego kontekstu.
     * Dane klienta podaje Handlowiec (customerId). Kreację deleguje do {@link OfferFactory};
     * oferta zostaje opublikowana ("Utworzona") i jest gotowa do prezentacji klientowi.
     */
    @Override
    public OfferId generateOffer(String customerId, String specificationId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank.");
        }
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId must not be blank.");
        }
        // UC-CRM-02, krok 3: cena katalogowa pochodzi z lokalnego read modelu zasilonego
        // zdarzeniem SpecificationCompleted (event-carried state transfer) — bez synchronicznego
        // odpytywania Katalogu. Brak wpisu = zdarzenie jeszcze nie dotarło.
        Money basePrice = specificationPriceReadModel.findPrice(new SpecificationId(specificationId))
                .orElseThrow(() -> new SpecificationNotFoundException(
                        "Brak wyceny dla specyfikacji " + specificationId
                                + " — zdarzenie SpecificationCompleted jeszcze nie dotarło."));

        Offer offer = offerFactory.createOffer(
                new CustomerId(customerId), new SpecificationId(specificationId), basePrice);
        offer.publishOffer();
        offerRepository.save(offer);
        return offer.getId();
    }

    /**
     * UC-CRM-02 (warunek wstępny): zapis wyceny katalogowej specyfikacji z otrzymanego
     * zdarzenia SpecificationCompleted do lokalnego read modelu Sprzedaży.
     */
    @Override
    public void registerSpecificationPrice(String specificationId, Money price) {
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId must not be blank.");
        }
        if (price == null) {
            throw new IllegalArgumentException("price must not be null.");
        }
        this.specificationPriceReadModel.saveSpecificationPrice(
                new SpecificationId(specificationId), price);
    }

    // --- UC-CRM-03 (cz.1): akceptacja oferty i utworzenie zamówienia ---

    /**
     * Reguły ważności i konwersji egzekwuje agregat Offer (accept/toSnapshot);
     * zamówienie buduje {@link OrderFactory} z niemutowalnej migawki oferty.
     * Po zapisie publikowana jest cała paczka zdarzeń agregatu (OrderPlacedEvent),
     * a Inwentarz dostaje zlecenie alokacji pojazdu/slotu produkcyjnego.
     */
    @Override
    public String acceptOfferAndCreateOrder(OfferId offerId) {
        if (offerId == null) {
            throw new IllegalArgumentException("offerId must not be null.");
        }
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new OfferNotFoundException(offerId.value()));

        offer.accept();
        Order order = orderFactory.createFromOffer(offer.getId(), offer.toSnapshot());

        offerRepository.save(offer);
        orderRepository.save(order);
        eventPublisher.publishAll(order.pullDomainEvents());

        if (inventoryPort != null) {
            inventoryPort.allocateVehicleOrProductionSlot(order.getId().value());
        }
        return order.getId().value();
    }

    // UC-CRM-03, scenariusz A1: klient odrzuca ofertę — nic nie jest emitowane.
    public void rejectOffer(OfferId offerId) {
        if (offerId == null) {
            throw new IllegalArgumentException("offerId must not be null.");
        }
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new OfferNotFoundException(offerId.value()));
        offer.reject();
        offerRepository.save(offer);
    }

    // --- UC-CRM-03 (cz.2): aktywacja po zaksięgowaniu wpłaty (Rys. 19/20 PDF) ---

    @Override
    public void activateOnDeposit(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        Optional<Order> found = orderRepository.findById(new OrderId(orderId));
        if (found.isEmpty()) {
            System.out.println("[SalesService] No order for id " + orderId + " — deposit ignored.");
            return;
        }
        Order order = found.get();
        if (order.getState() != OrderState.DRAFT_CREATED && order.getState() != OrderState.DRAFT) {
            System.out.println("[SalesService] Order " + orderId
                    + " already active (" + order.getState() + ") — deposit event ignored.");
            return;
        }
        order.activate();
        orderRepository.save(order);
        eventPublisher.publishAll(order.pullDomainEvents());
    }

    // --- UC-CRM-04: gotowość pojazdu i umówienie odbioru ---

    /**
     * Krok 1-2: reakcja na VehicleReadyForHandoverEvent z Inwentarza — zamówienie przechodzi
     * w "Gotowe do odbioru" i publikuje OrderReadyForHandoverEvent (powiadomienie Handlowca).
     */
    public void markOrderAsReadyForHandover(OrderId orderId) {
        Order order = loadOrder(orderId);
        order.markAsReadyForHandover();
        orderRepository.save(order);
        eventPublisher.publishAll(order.pullDomainEvents());
    }

    /** Krok 4-5: Handlowiec wprowadza uzgodniony termin odbioru -> "Umówiony na odbiór". */
    @Override
    public void scheduleHandover(ScheduleHandoverCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        Order order = loadOrder(new OrderId(command.orderId()));
        // Walidacja wejścia — błędna data odpada, zanim cokolwiek trafi do bazy (UC-CRM-04).
        if (command.handoverDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Handover date cannot be in the past");
        }
        order.scheduleHandover(command.handoverDate());
        orderRepository.save(order);
        eventPublisher.publishAll(order.pullDomainEvents());
    }

    // --- UC-CRM-05: rejestracja fizycznego wydania pojazdu ---

    /**
     * Zamówienie przechodzi w "Zrealizowane" (Order.confirmHandover), Inwentarz dostaje
     * komendę ReleaseVehicle (zdjęcie fizycznego auta ze stanu — UC-INW-06), a Rozliczenia
     * domykają saldo końcowe. Błąd Inwentarza przerywa proces PRZED zapisem (zamówienie
     * nie może zostać oznaczone jako zrealizowane przy blokadzie magazynowej).
     */
    @Override
    public void confirmHandover(OrderId orderId) {
        Order order = loadOrder(orderId);
        order.confirmHandover();

        if (inventoryPort != null) {
            inventoryPort.releasePhysicalVehicle(order.getVehicleId());
        }
        if (billingPort != null) {
            billingPort.closeOrderBalance(order.getId().value());
        }

        orderRepository.save(order);
        eventPublisher.publishAll(order.pullDomainEvents());
    }

    /**
     * UC-CRM-05, A1: Inwentarz odmówił zwolnienia (VehicleInventoryReleasedError) — kompensata
     * (saga): cofnięcie zamówienia do "Gotowe do odbioru" (data wydania jest czyszczona).
     */
    public void revertHandoverOnInventoryError(String orderId) {
        Order order = loadOrder(new OrderId(orderId));
        order.revertToReadyForHandover();
        orderRepository.save(order);
        eventPublisher.publishAll(order.pullDomainEvents());
    }

    // --- Anulowanie zamówienia (rezygnacja klienta) ---

    public void cancelOrder(String orderId, String reason) {
        Order order = loadOrder(new OrderId(orderId));
        order.cancelOrder(reason);
        orderRepository.save(order);
        eventPublisher.publishAll(order.pullDomainEvents());
    }

    // --- Cron: wygaszanie przeterminowanych ofert (validityDate < dziś) ---

    /**
     * Oferty z przekroczoną datą ważności nie mogą już zostać zaakceptowane (regułę
     * egzekwuje też agregat w accept()); zadanie cykliczne odrzuca przeterminowane,
     * opublikowane oferty, aby nie zalegały w aktywnym obiegu handlowym.
     */
    public void expireOutdatedOffers() {
        LocalDate today = LocalDate.now();
        List<Offer> offers = offerRepository.findAll();
        for (int i = 0; i < offers.size(); i++) {
            Offer offer = offers.get(i);
            if (offer.getState() == OfferState.PUBLISHED
                    && offer.getValidityDate().isBefore(today)) {
                offer.reject();
                offerRepository.save(offer);
            }
        }
    }

    // --- pomocnicze ---

    private Order loadOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with ID " + orderId.value() + " not found"));
    }
}
