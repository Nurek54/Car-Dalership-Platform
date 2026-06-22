package unit.sales_and_crm_context.factories;

import org.junit.jupiter.api.Test;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.offer.OfferSnapshot;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.domain.model.order.OrderFactory;
import salon.common.model.*;

import static org.assertj.core.api.Assertions.*;

class OrderFactoryTest {

    private final OrderFactory orderFactory = new OrderFactory();

    @Test
    void shouldCreateOrderFromValidOfferSnapshot() {
        OfferId offerId = new OfferId("O-123");
        OfferSnapshot validSnapshot = new OfferSnapshot(
                offerId,
                new CustomerId("C-999"),
                new SpecificationId("SPEC-1"),
                Money.of(200000, "PLN")
        );

        // Fabryka buduje zamówienie
        Order order = orderFactory.createFromOffer(offerId, validSnapshot);

        assertThat(order).isNotNull();
        assertThat(order.id()).isNotNull();
        assertThat(order.offerId()).isEqualTo(offerId);
        assertThat(order.requiredDeposit()).isEqualTo(Money.of(200000, "PLN"));
    }

    @Test
    void shouldRejectCreationIfSnapshotOfferIdDoesNotMatch() {
        // Migawka należy do innej oferty niż zadeklarowano
        OfferId actualOfferId = new OfferId("O-123");
        OfferSnapshot mismatchedSnapshot = new OfferSnapshot(
                new OfferId("O-HACKER-999"),
                new CustomerId("C-999"),
                new SpecificationId("SPEC-1"),
                Money.of(200000, "PLN")
        );

        assertThatThrownBy(() -> orderFactory.createFromOffer(actualOfferId, mismatchedSnapshot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Snapshot offerId does not match the provided offerId");
    }
}