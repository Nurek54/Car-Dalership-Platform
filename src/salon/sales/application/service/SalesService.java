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

    

    @Override
    public String acceptOffer(AcceptOfferCommand command) {
        Offer offer = this.offerRepository.findById(new OfferId(command.offerId()))
                .orElseThrow(() -> new OfferNotFoundException(command.offerId()));
        offer.accept();                                   
        this.offerRepository.save(offer);

        Order order = this.orderFactory.createFromOffer(offer);
        order.activate();                                 
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

    
    public String acceptOfferAndCreateOrder(OfferId offerId) {
        Offer offer = this.offerRepository.findById(offerId)
                .orElseThrow(() -> new OfferNotFoundException(offerId.value()));
        offer.accept();
        this.offerRepository.save(offer);

        Order order = this.orderFactory.createFromOffer(offer);
        this.orderRepository.save(order);                 

        List<DomainEvent> events = new ArrayList<>();
        events.add(new OrderPlacedEvent(order.getId().value(), offer.getId().value(),
                offer.getSpecificationId().value(), offer.getCustomerId().value()));
        events.addAll(order.pullDomainEvents());
        this.eventPublisher.publishAll(events);
        return order.getId().value();
    }

    

    @Override
    public void activateOnDeposit(String orderId) {
        Order order = loadOrder(orderId);
        if (order.state() == salon.sales.application.domain.model.order.OrderState.DRAFT_CREATED) {
            order.activate();
            this.orderRepository.save(order);
            this.eventPublisher.publishAll(order.pullDomainEvents());
        }
    }

    

    
    public void markReadyForHandover(String orderId) {
        markOrderAsReadyForHandover(new OrderId(orderId));
    }

    
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

    

    @Override
    public void releaseVehicle(String orderId) {
        confirmHandover(new OrderId(orderId));
    }

    
    public void confirmHandover(OrderId orderId) {
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId.value() + " not found"));
        order.confirmHandover();                          
        this.inventoryIntegration.releasePhysicalVehicle(orderId.value()); 
        this.orderRepository.save(order);

        List<DomainEvent> events = new ArrayList<>(order.pullDomainEvents());
        events.add(new VehicleHandedOverEvent(orderId.value()));
        this.eventPublisher.publishAll(events);
    }

    
    public void revertHandoverOnInventoryError(String orderId) {
        Order order = loadOrder(orderId);
        order.revertToReadyForHandover();
        this.orderRepository.save(order);
    }

    
    public void cancelOrder(OrderId orderId, String reason) {
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId.value() + " not found"));
        order.cancel(reason);
        this.orderRepository.save(order);
        this.billingIntegration.processCancelledOrderBilling(orderId.value(), reason);
        this.eventPublisher.publishAll(order.pullDomainEvents());
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
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found"));
    }
}
