package salon.sales.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.common.model.SpecificationId;
import salon.sales.application.command.AcceptOfferCommand;
import salon.sales.application.command.ScheduleHandoverCommand;
import salon.sales.application.domain.event.OfferCreatedEvent;
import salon.sales.application.domain.event.OrderPlacedEvent;
import salon.sales.application.domain.event.VehicleHandedOverEvent;
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
import salon.sales.application.domain.model.order.PaymentMethod;
import salon.sales.application.port.in.AcceptOffer;
import salon.sales.application.port.in.ActivateOrderOnDeposit;
import salon.sales.application.port.in.ExpireOutdatedOffer;
import salon.sales.application.port.in.ReleaseVehicle;
import salon.sales.application.port.in.ScheduleHandover;
import salon.sales.application.port.out.BillingIntegration;
import salon.sales.application.port.out.CustomerDatabaseRepository;
import salon.sales.application.port.out.InventoryIntegration;
import salon.sales.application.port.out.OfferDatabaseRepository;
import salon.sales.application.port.out.OrderDatabaseRepository;
import salon.sales.application.port.out.SpecificationPriceReadModelPort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * APPLICATION SERVICE (Figure 22) — "SalesService". The central orchestration point of the
 * Sales and CRM Context. Realizes the inbound ports {@link AcceptOffer}, {@link ActivateOrderOnDeposit},
 * {@link ScheduleHandover}, {@link ReleaseVehicle} and {@link ExpireOutdatedOffer}, plus the
 * event-driven operations (UC-CRM-02/04/05) invoked by the inbound EventListener adapters.
 * Business rules live in the aggregates; this service only orchestrates and publishes events.
 */
@Service
public class SalesService implements
        AcceptOffer, ActivateOrderOnDeposit, ScheduleHandover, ReleaseVehicle, ExpireOutdatedOffer {

    private final CustomerDatabaseRepository customerRepository;
    private final OfferDatabaseRepository offerRepository;
    private final OrderDatabaseRepository orderRepository;
    private final BillingIntegration billingIntegration;
    private final InventoryIntegration inventoryIntegration;
    private final EventPublisher eventPublisher;
    private final OfferFactory offerFactory;
    private final OrderFactory orderFactory;
    private final SpecificationPriceReadModelPort specificationPriceReadModel;

    @Autowired
    public SalesService(CustomerDatabaseRepository customerRepository,
                        OfferDatabaseRepository offerRepository,
                        OrderDatabaseRepository orderRepository,
                        BillingIntegration billingIntegration,
                        InventoryIntegration inventoryIntegration,
                        EventPublisher eventPublisher,
                        OfferFactory offerFactory,
                        OrderFactory orderFactory,
                        SpecificationPriceReadModelPort specificationPriceReadModel) {
        this.customerRepository = customerRepository;
        this.offerRepository = offerRepository;
        this.orderRepository = orderRepository;
        this.billingIntegration = billingIntegration;
        this.inventoryIntegration = inventoryIntegration;
        this.eventPublisher = eventPublisher;
        this.offerFactory = offerFactory;
        this.orderFactory = orderFactory;
        this.specificationPriceReadModel = specificationPriceReadModel;
    }

    /** Backward-compatible constructor (no read model) used by the legacy POJO wiring/demos. */
    public SalesService(CustomerDatabaseRepository customerRepository,
                        OfferDatabaseRepository offerRepository,
                        OrderDatabaseRepository orderRepository,
                        BillingIntegration billingIntegration,
                        InventoryIntegration inventoryIntegration,
                        EventPublisher eventPublisher,
                        OfferFactory offerFactory,
                        OrderFactory orderFactory) {
        this(customerRepository, offerRepository, orderRepository, billingIntegration,
                inventoryIntegration, eventPublisher, offerFactory, orderFactory, null);
    }

    /** Registers a customer (CRM master data) needed before building offers. */
    public void registerCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null.");
        }
        this.customerRepository.save(customer);
    }

    // ===== UC-CRM-02: generate the proforma offer =====

    /**
     * UC-CRM-02: a completed specification (with the catalog price) arrived from the Catalog Context;
     * a proforma offer is created and published. Returns the new offer id.
     */
    public String createProformaOffer(String customerId, String specificationId, Money basePrice) {
        Offer offer = this.offerFactory.createProforma(
                new CustomerId(customerId), new SpecificationId(specificationId), basePrice);
        offer.publishOffer();
        this.offerRepository.save(offer);
        this.eventPublisher.publish(new OfferCreatedEvent(offer.getId().value(), customerId, specificationId));
        return offer.getId().value();
    }

    /**
     * UC-CRM-02: generates and publishes a proforma offer using the price already cached in the
     * local read model (fed by the Catalog's SpecificationCompleted event). No price -> no offer.
     */
    public String generateOffer(String customerId, String specificationId) {
        SpecificationId specId = new SpecificationId(specificationId);
        Money price = this.specificationPriceReadModel.findPrice(specId)
                .orElseThrow(() -> new SpecificationNotFoundException(
                        "Specification " + specificationId + " price is not yet available in the read model"));
        Offer offer = this.offerFactory.createOffer(new CustomerId(customerId), specId, price);
        offer.publishOffer();
        this.offerRepository.save(offer);
        this.eventPublisher.publish(new OfferCreatedEvent(offer.getId().value(), customerId, specificationId));
        return offer.getId().value();
    }

    // ===== AcceptOffer: UC-CRM-03 =====

    @Override
    public String acceptOffer(AcceptOfferCommand command) {
        Offer offer = this.offerRepository.findById(new OfferId(command.offerId()))
                .orElseThrow(() -> new OfferNotFoundException(command.offerId()));
        offer.accept();                                   // PUBLISHED -> ACCEPTED (rule in the aggregate)
        this.offerRepository.save(offer);

        Order order = this.orderFactory.createFromOffer(offer);
        order.activate();                                 // DRAFT_CREATED -> IN_PROGRESS
        order.declarePaymentMethod(command.paymentMethod());
        this.orderRepository.save(order);

        String orderId = order.getId().value();
        this.billingIntegration.openSettlement(orderId, offer.getFinalPrice());

        List<DomainEvent> events = new ArrayList<>();
        events.add(new OrderPlacedEvent(orderId, offer.getId().value(),
                offer.getSpecificationId().value(), offer.getCustomerId().value()));
        events.addAll(order.pullDomainEvents());
        this.eventPublisher.publishAll(events);
        return orderId;
    }

    /**
     * UC-CRM-03 (driven from the REST adapter): accepts a published offer and creates the order.
     * The order is left in DRAFT_CREATED until the deposit/financing path activates it.
     */
    public String acceptOfferAndCreateOrder(OfferId offerId) {
        Offer offer = this.offerRepository.findById(offerId)
                .orElseThrow(() -> new OfferNotFoundException(offerId.value()));
        offer.accept();
        this.offerRepository.save(offer);

        Order order = this.orderFactory.createFromOffer(offer);
        this.orderRepository.save(order);                 // may throw DatabaseException -> no events published

        List<DomainEvent> events = new ArrayList<>();
        events.add(new OrderPlacedEvent(order.getId().value(), offer.getId().value(),
                offer.getSpecificationId().value(), offer.getCustomerId().value()));
        events.addAll(order.pullDomainEvents());
        this.eventPublisher.publishAll(events);
        return order.getId().value();
    }

    // ===== ActivateOrderOnDeposit: UC-CRM-03 (part 2) =====

    @Override
    public void activateOnDeposit(String orderId) {
        Order order = loadOrder(orderId);
        if (order.state() == salon.sales.application.domain.model.order.OrderState.DRAFT_CREATED) {
            order.activate();
            this.orderRepository.save(order);
            this.eventPublisher.publishAll(order.pullDomainEvents());
        }
    }

    // ===== UC-CRM-04 =====

    /** The vehicle is physically and financially ready — mark the order ready for handover. */
    public void markReadyForHandover(String orderId) {
        markOrderAsReadyForHandover(new OrderId(orderId));
    }

    /** UC-CRM-04: marks the order ready for handover (driven by VehicleReadyForHandover). */
    public void markOrderAsReadyForHandover(OrderId orderId) {
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId.value() + " not found"));
        order.markAsReadyForHandover();
        this.orderRepository.save(order);
        this.eventPublisher.publishAll(order.pullDomainEvents());
    }

    @Override
    public void scheduleHandover(ScheduleHandoverCommand command) {
        if (command.handoverDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Handover date cannot be in the past");
        }
        Order order = this.orderRepository.findById(new OrderId(command.orderId()))
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with ID " + command.orderId() + " not found"));
        order.scheduleHandover(command.handoverDate());
        this.orderRepository.save(order);
        this.eventPublisher.publishAll(order.pullDomainEvents());
    }

    // ===== ReleaseVehicle / confirm handover: UC-CRM-05 =====

    @Override
    public void releaseVehicle(String orderId) {
        confirmHandover(new OrderId(orderId));
    }

    /** UC-CRM-05: confirms the physical handover, releases the vehicle and completes the order. */
    public void confirmHandover(OrderId orderId) {
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId.value() + " not found"));
        order.confirmHandover();                          // -> COMPLETED (in memory)
        this.inventoryIntegration.releasePhysicalVehicle(orderId.value()); // may throw -> no save/publish
        this.orderRepository.save(order);

        List<DomainEvent> events = new ArrayList<>(order.pullDomainEvents());
        events.add(new VehicleHandedOverEvent(orderId.value()));
        this.eventPublisher.publishAll(events);
    }

    /** UC-CRM-05 / A1: Inventory rejected the release — revert the order to READY_FOR_HANDOVER. */
    public void revertHandoverOnInventoryError(String orderId) {
        Order order = loadOrder(orderId);
        order.revertToReadyForHandover();
        this.orderRepository.save(order);
    }

    /** UC-CRM-03 (A1): cancels the order and notifies Billing/external systems. */
    public void cancelOrder(OrderId orderId, String reason) {
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId.value() + " not found"));
        order.cancel(reason);
        this.orderRepository.save(order);
        this.billingIntegration.processCancelledOrderBilling(orderId.value(), reason);
        this.eventPublisher.publishAll(order.pullDomainEvents());
    }

    // ===== ExpireOutdatedOffer: CronJob =====

    @Override
    public void expireOutdatedOffers() {
        LocalDate today = LocalDate.now();
        for (Offer offer : this.offerRepository.findAll()) {
            if (offer.getState() == OfferState.PUBLISHED && offer.getValidityDate().isBefore(today)) {
                offer.reject();
                this.offerRepository.save(offer);
            }
        }
    }

    private Order loadOrder(String orderId) {
        return this.orderRepository.findById(new OrderId(orderId))
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found"));
    }
}
