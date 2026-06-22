package salon.sales.application.service;

import org.springframework.stereotype.Service;
import salon.common.model.OrderId;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.OfferSnapshotDto;
import salon.sales.api.SalesQueryFacade;
import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.port.out.CustomerDatabaseRepository;
import salon.sales.application.port.out.OfferDatabaseRepository;
import salon.sales.application.port.out.OrderDatabaseRepository;

/**
 * APPLICATION SERVICE (Figure 22) — "SalesQueryService". Realizes the public {@link SalesQueryFacade}
 * for synchronous cross-context queries; performs the order -> offer -> customer navigation and
 * returns Published-Language snapshots (no aggregates leak out).
 */
@Service
public class SalesQueryService implements SalesQueryFacade {

    private final OrderDatabaseRepository orderRepository;
    private final OfferDatabaseRepository offerRepository;
    private final CustomerDatabaseRepository customerRepository;

    public SalesQueryService(OrderDatabaseRepository orderRepository,
                             OfferDatabaseRepository offerRepository,
                             CustomerDatabaseRepository customerRepository) {
        if (orderRepository == null) {
            throw new IllegalArgumentException("orderRepository must not be null.");
        }
        if (offerRepository == null) {
            throw new IllegalArgumentException("offerRepository must not be null.");
        }
        if (customerRepository == null) {
            throw new IllegalArgumentException("customerRepository must not be null.");
        }
        this.orderRepository = orderRepository;
        this.offerRepository = offerRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerSnapshotDto findBuyerForOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        Offer offer = sourceOfferOf(orderId);
        Customer customer = this.customerRepository.findById(offer.getCustomerId())
                .orElseThrow(() -> new IllegalStateException(
                        "No customer for offer " + offer.getId()));
        return new CustomerSnapshotDto(customer.getFullName(), customer.getNip());
    }

    @Override
    public OfferSnapshotDto findOfferForOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        Offer offer = sourceOfferOf(orderId);
        return new OfferSnapshotDto(offer.getId().value(), offer.getFinalPrice());
    }

    private Offer sourceOfferOf(OrderId orderId) {
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("No order " + orderId.value()));
        return this.offerRepository.findById(order.getSourceOfferId())
                .orElseThrow(() -> new IllegalStateException(
                        "No source offer " + order.getSourceOfferId() + " for order " + orderId.value()));
    }
}
