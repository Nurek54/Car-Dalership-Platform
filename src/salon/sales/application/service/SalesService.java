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

    public void registerCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null.");
        }
        this.customerRepository.save(customer);
    }

    public String createProformaOffer(String customerId, String specificationId, Money basePrice) {
        Offer offer = this.offerFactory.createProforma(
                new CustomerId(customerId), new SpecificationId(specificationId), basePrice);
        offer.publishOffer();
        this.offerRepository.save(offer);
        this.eventPublisher.publish(new OfferCreatedEvent(offer.getId().value(), customerId, specificationId));
        return offer.getId().value();
    }

    @Override
    public String acceptOffer(AcceptOfferCommand command) {
        Offer offer = this.offerRepository.findById(new OfferId(command.offerId()))
                .orElseThrow(() -> new IllegalStateException("No offer " + command.offerId()));
        offer.accept();
        this.offerRepository.save(offer);

        Order order = this.orderFactory.createFromOffer(offer);
        order.declarePaymentMethod(command.paymentMethod());
        this.orderRepository.save(order);

        String orderId = order.getId().value();
        this.billingIntegration.openSettlement(orderId, offer.getFinalPrice());
        this.eventPublisher.publish(new OrderPlacedEvent(
                orderId, offer.getId().value(), offer.getSpecificationId().value(),
                offer.getCustomerId().value()));

        if (command.paymentMethod() == PaymentMethod.BANK_TRANSFER) {
            this.eventPublisher.publish(new BankTransferDeclaredEvent(orderId));
        } else {
            this.eventPublisher.publish(new FinancingRequestedEvent(orderId, offer.getCustomerId().value()));
        }
        return orderId;
    }

    @Override
    public void activateOnDeposit(String orderId) {
        loadOrder(orderId);
    }

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

    @Override
    public void releaseVehicle(String orderId) {
        Order order = loadOrder(orderId);
        order.confirmHandover();
        this.orderRepository.save(order);
        this.inventoryIntegration.releaseVehicle(orderId);
    }

    public void revertHandoverOnInventoryError(String orderId) {
        Order order = loadOrder(orderId);
        order.revertToReadyForHandover();
        this.orderRepository.save(order);
    }

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
