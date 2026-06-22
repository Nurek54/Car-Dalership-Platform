package salon.sales.application.service;

import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.common.model.SpecificationId;
import salon.sales.application.command.AcceptOfferCommand;
import salon.sales.application.command.ScheduleHandoverCommand;
import salon.sales.application.domain.event.BankTransferDeclaredEvent;
import salon.sales.application.domain.event.FinancingRequestedEvent;
import salon.sales.application.domain.event.OfferCreatedEvent;
import salon.sales.application.domain.event.OrderPlacedEvent;
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

import java.time.LocalDate;

/**
 * APPLICATION SERVICE (Figure 22) — "SalesService". The central orchestration point of the
 * Sales and CRM Context. Realizes the inbound ports {@link AcceptOffer}, {@link ActivateOrderOnDeposit},
 * {@link ScheduleHandover}, {@link ReleaseVehicle} and {@link ExpireOutdatedOffer}, and exposes the
 * event-driven operations (UC-CRM-02/04/05) invoked by the inbound EventListener adapters.
 * Business rules live in the aggregates; this service only orchestrates and publishes events.
 */
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

    public SalesService(CustomerDatabaseRepository customerRepository,
                        OfferDatabaseRepository offerRepository,
                        OrderDatabaseRepository orderRepository,
                        BillingIntegration billingIntegration,
                        InventoryIntegration inventoryIntegration,
                        EventPublisher eventPublisher,
                        OfferFactory offerFactory,
                        OrderFactory orderFactory) {
        if (customerRepository == null || offerRepository == null || orderRepository == null
                || billingIntegration == null || inventoryIntegration == null || eventPublisher == null
                || offerFactory == null || orderFactory == null) {
            throw new IllegalArgumentException("SalesService dependencies must not be null.");
        }
        this.customerRepository = customerRepository;
        this.offerRepository = offerRepository;
        this.orderRepository = orderRepository;
        this.billingIntegration = billingIntegration;
        this.inventoryIntegration = inventoryIntegration;
        this.eventPublisher = eventPublisher;
        this.offerFactory = offerFactory;
        this.orderFactory = orderFactory;
    }

    /** Registers a customer (CRM master data) needed before building offers. */
    public void registerCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null.");
        }
        this.customerRepository.save(customer);
    }

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

    // ===== AcceptOffer: UC-CRM-03 =====

    @Override
    public String acceptOffer(AcceptOfferCommand command) {
        Offer offer = this.offerRepository.findById(new OfferId(command.offerId()))
                .orElseThrow(() -> new IllegalStateException("No offer " + command.offerId()));
        offer.accept();                                   // PUBLISHED -> ACCEPTED (rule in the aggregate)
        this.offerRepository.save(offer);

        Order order = this.orderFactory.createFromOffer(offer);
        order.declarePaymentMethod(command.paymentMethod());
        this.orderRepository.save(order);

        String orderId = order.getId().value();
        this.billingIntegration.openSettlement(orderId, offer.getFinalPrice());
        this.eventPublisher.publish(new OrderPlacedEvent(
                orderId, offer.getId().value(), offer.getSpecificationId().value(),
                offer.getCustomerId().value()));

        // UC-CRM-03, step 5: branch on the declared payment method.
        if (command.paymentMethod() == PaymentMethod.BANK_TRANSFER) {
            this.eventPublisher.publish(new BankTransferDeclaredEvent(orderId));
        } else {
            this.eventPublisher.publish(new FinancingRequestedEvent(orderId, offer.getCustomerId().value()));
        }
        return orderId;
    }

    // ===== ActivateOrderOnDeposit: UC-CRM-03 (part 2) =====

    @Override
    public void activateOnDeposit(String orderId) {
        // The deposit has been registered by Billing; the order is confirmed active.
        // Vehicle reservation / factory order are driven by Billing/Inventory on their own events;
        // here we only assert the order exists (no further state change in the lean Order model).
        loadOrder(orderId);
    }

    // ===== UC-CRM-04 (driven by VehicleReadyForHandover from Inventory) =====

    /** The vehicle is physically and financially ready — mark the order ready for handover. */
    public void markReadyForHandover(String orderId) {
        Order order = loadOrder(orderId);
        order.markAsReady();
        this.orderRepository.save(order);
    }

    @Override
    public void scheduleHandover(ScheduleHandoverCommand command) {
        Order order = loadOrder(command.orderId());
        order.scheduleHandover(command.handoverDate());
        this.orderRepository.save(order);
    }

    // ===== ReleaseVehicle: UC-CRM-05 =====

    @Override
    public void releaseVehicle(String orderId) {
        Order order = loadOrder(orderId);
        order.confirmHandover();                          // HANDOVER_SCHEDULED -> COMPLETED
        this.orderRepository.save(order);
        this.inventoryIntegration.releaseVehicle(orderId);
    }

    /** UC-CRM-05 / A1: Inventory rejected the release — revert the order to READY_FOR_HANDOVER. */
    public void revertHandoverOnInventoryError(String orderId) {
        Order order = loadOrder(orderId);
        order.revertToReadyForHandover();
        this.orderRepository.save(order);
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
                .orElseThrow(() -> new IllegalStateException("No order " + orderId));
    }
}
