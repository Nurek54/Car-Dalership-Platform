package salon.sales.application.service;

import salon.sales.application.port.in.ActivateOrderOnDepositUseCase;
import salon.sales.application.port.in.CancelOrderCommand;
import salon.sales.application.port.in.CancelOrderUseCase;
import salon.sales.application.port.in.CompleteHandoverUseCase;
import salon.sales.application.port.in.CreateOrderCommand;
import salon.sales.application.port.in.ScheduleHandoverCommand;
import salon.sales.application.port.in.ScheduleHandoverUseCase;
import salon.sales.application.port.out.OfferRepository;
import salon.sales.application.port.out.OrderRepository;
import salon.sales.domain.exceptions.OfferExpiredException;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.offer.OfferState;
import salon.sales.domain.model.order.Order;
import salon.sales.domain.model.order.OrderFactory;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.OrderId;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Realizuje UC-SPR-02 (konwersja oferty, aktywacja po zadatku) i UC-SPR-03 (anulowanie).
 * Publikuje zdarzenia WYGENEROWANE PRZEZ AGREGAT — decyzja "jakie zdarzenie" siedzi w domenie.
 *
 * Zamówienie z oferty buduje {@link OrderFactory} z niemutowalnej migawki oferty
 * ({@code Offer#toSnapshot()}) — nie przekazujemy referencji do agregatu Offer (patrz
 * docs/Agregate/Sales/order.md i docs/Agregate/Guidelines/value-object-audit.md).
 *
 * OfferRepository jest potrzebne tylko dla createOrderFromOffer. Stary, 2-argumentowy
 * konstruktor zostaje (zgodność z demami); createOrderFromOffer wymaga wariantu z OfferRepository.
 */
public class OrderAppService implements CancelOrderUseCase, ActivateOrderOnDepositUseCase, ScheduleHandoverUseCase, CompleteHandoverUseCase {

    private final OrderRepository orderRepository;
    private final OfferRepository offerRepository; // może być null (konstruktor 2-arg)
    private final EventPublisherPort eventPublisher;
    private final OrderFactory orderFactory;

    public OrderAppService(OrderRepository orderRepository, EventPublisherPort eventPublisher) {
        this(orderRepository, null, eventPublisher);
    }

    public OrderAppService(OrderRepository orderRepository,
                           OfferRepository offerRepository,
                           EventPublisherPort eventPublisher) {
        this(orderRepository, offerRepository, eventPublisher, new OrderFactory());
    }

    public OrderAppService(OrderRepository orderRepository,
                           OfferRepository offerRepository,
                           EventPublisherPort eventPublisher,
                           OrderFactory orderFactory) {
        if (orderRepository == null) {
            throw new IllegalArgumentException("orderRepository must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        if (orderFactory == null) {
            throw new IllegalArgumentException("orderFactory must not be null.");
        }
        this.orderRepository = orderRepository;
        this.offerRepository = offerRepository;
        this.eventPublisher = eventPublisher;
        this.orderFactory = orderFactory;
    }

    /**
     * UC-SPR-02: konwersja Oferty w Zamówienie. Zwraca identyfikator nowego zamówienia.
     * Reguła: oferta po terminie ważności -> OfferExpiredException; oferta musi być PUBLISHED.
     */
    public String createOrderFromOffer(CreateOrderCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        if (offerRepository == null) {
            throw new IllegalStateException(
                    "OrderAppService was built without an OfferRepository — "
                            + "use the 3-arg constructor to enable createOrderFromOffer.");
        }

        Optional<Offer> foundOffer = offerRepository.findById(new OfferId(command.offerId()));
        if (foundOffer.isEmpty()) {
            throw new IllegalStateException("Offer not found: " + command.offerId());
        }
        Offer offer = foundOffer.get();

        if (offer.getState() == OfferState.EXPIRED
                || offer.getValidityDate().isBefore(LocalDate.now())) {
            throw new OfferExpiredException("Offer " + command.offerId() + " has expired.");
        }
        // Reguła konwersji (przeniesiona z domeny do orkiestracji, bo fabryka działa na migawce).
        if (offer.getState() != OfferState.PUBLISHED) {
            throw new IllegalStateException("Order can only be created from a PUBLISHED offer.");
        }

        Order order = orderFactory.createFromOffer(offer.getId(), offer.toSnapshot());
        order.confirmSignature(command.customerSignature());
        orderRepository.save(order);

        offer.markAsConverted();
        offerRepository.save(offer);

        return order.getId().value();
    }

    // UC-SPR-02, krok 5: ZadatekZaksiegowany -> aktywacja zamówienia + ogłoszenie ZamowienieAktywowane.
    @Override
    public void activateOnDeposit(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        Optional<Order> found = orderRepository.findById(new OrderId(orderId));
        if (found.isEmpty()) {
            System.out.println("[OrderAppService] No order for id " + orderId + " — deposit ignored.");
            return;
        }
        Order order = found.get();
        order.activate();
        orderRepository.save(order);

        publishEventsOf(order);
    }

    /**
     * Aktywacja zamówienia po odebraniu DepositRegisteredEvent (BillingEventSubscriberAdapter).
     * W odróżnieniu od activateOnDeposit — brak zamówienia traktujemy jako błąd (wiadomość do DLQ).
     */
    public void activateOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        Optional<Order> found = orderRepository.findById(new OrderId(orderId));
        if (found.isEmpty()) {
            throw new IllegalStateException("Order not found for activation: " + orderId);
        }
        Order order = found.get();
        order.activate();
        orderRepository.save(order);
        publishEventsOf(order);
    }

    /**
     * UC-CRM-04, krok 1-2: reakcja na VehicleReadyForHandoverEvent z Inwentarza/Logistyki.
     * Zamówienie przechodzi w stan READY_FOR_HANDOVER i publikuje OrderReadyForHandoverEvent
     * (powiadomienie Handlowca). Brak zamówienia traktujemy jako błąd (wiadomość do DLQ).
     */
    public void markReadyForHandover(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        Optional<Order> found = orderRepository.findById(new OrderId(orderId));
        if (found.isEmpty()) {
            throw new IllegalStateException("Order not found for handover readiness: " + orderId);
        }
        Order order = found.get();
        order.markAsReadyForHandover();
        orderRepository.save(order);
        publishEventsOf(order);
    }

    /**
     * UC-CRM-04, krok 4-5: Handlowiec ustala termin odbioru -> stan HANDOVER_SCHEDULED.
     */
    @Override
    public void scheduleHandover(ScheduleHandoverCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        Optional<Order> found = orderRepository.findById(new OrderId(command.orderId()));
        if (found.isEmpty()) {
            throw new IllegalStateException("Order not found: " + command.orderId());
        }
        Order order = found.get();
        order.scheduleHandover(command.handoverDate());
        orderRepository.save(order);
        publishEventsOf(order);
    }

    /**
     * UC-CRM-05: rejestracja fizycznego wydania pojazdu. Zamówienie przechodzi w COMPLETED i
     * publikuje VehicleHandedOverEvent — sygnał "ReleaseVehicle" dla Inwentarza (zwolnienie
     * pojazdu) oraz dla Rozliczeń (domknięcie salda).
     */
    @Override
    public void completeHandover(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        Optional<Order> found = orderRepository.findById(new OrderId(orderId));
        if (found.isEmpty()) {
            throw new IllegalStateException("Order not found: " + orderId);
        }
        Order order = found.get();
        order.completeHandover();
        orderRepository.save(order);
        publishEventsOf(order);
    }

    /**
     * UC-CRM-05, A1: Inwentarz odmówił zwolnienia (VehicleInventoryReleasedError) — kompensata:
     * cofnięcie zamówienia do READY_FOR_HANDOVER. Brak zamówienia traktujemy jako błąd (DLQ).
     */
    public void revertHandoverOnInventoryError(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        Optional<Order> found = orderRepository.findById(new OrderId(orderId));
        if (found.isEmpty()) {
            throw new IllegalStateException("Order not found for handover revert: " + orderId);
        }
        Order order = found.get();
        order.revertToReadyForHandover();
        orderRepository.save(order);
        publishEventsOf(order);
    }

    // UC-SPR-03: anulowanie + (wg winy) polecenie do Rozliczeń: zatrzymaj zadatek / zleć zwrot.
    @Override
    public void cancelOrder(CancelOrderCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        Optional<Order> found = orderRepository.findById(new OrderId(command.orderId()));
        if (found.isEmpty()) {
            throw new IllegalStateException("Order not found: " + command.orderId());
        }
        Order order = found.get();

        // Cała reguła (wydanie + wybór zdarzenia wg winy) jest w agregacie — tu tylko orkiestrujemy.
        order.cancelOrder(command.reason(), command.isHandedOver());
        orderRepository.save(order);

        publishEventsOf(order);
    }

    private void publishEventsOf(Order order) {
        for (DomainEvent event : order.pullDomainEvents()) {
            eventPublisher.publish(event);
        }
    }
}
