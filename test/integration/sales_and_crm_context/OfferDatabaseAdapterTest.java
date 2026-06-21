package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import salon.sales.infrastructure.out.persistence.OfferDatabaseAdapter;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferState;
import salon.sales.application.domain.model.offer.OfferId;
import salon.common.model.CustomerId;
import salon.common.model.SpecificationId;
import salon.common.model.Money;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(OfferDatabaseAdapter.class)
class OfferDatabaseAdapterTest {

    @Autowired private OfferDatabaseAdapter databaseAdapter;

    @Test
    void shouldSaveAndLoadDraftOffer() {
        OfferId offerId = new OfferId("OFF-DB-1");
        Offer offer = new Offer(offerId, new CustomerId("C-1"), new SpecificationId("S-1"), Money.of(150000, "PLN"));

        databaseAdapter.save(offer);
        Optional<Offer> loadedOffer = databaseAdapter.findById(offerId);

        // The saved data is the same on read
        assertThat(loadedOffer).isPresent();
        assertThat(loadedOffer.get().state()).isEqualTo(OfferState.DRAFT);
        assertThat(loadedOffer.get().finalPrice()).isEqualTo(Money.of(150000, "PLN"));
    }

    @Test
    void shouldUpdateOfferStateToPublished() {
        // We have a saved offer
        OfferId offerId = new OfferId("OFF-DB-2");
        Offer offer = new Offer(offerId, new CustomerId("C-2"), new SpecificationId("S-2"), Money.of(200000, "PLN"));
        databaseAdapter.save(offer);

        // We fetch it, change its state (publish) and save
        Offer savedOffer = databaseAdapter.findById(offerId).orElseThrow();
        savedOffer.publishOffer();
        databaseAdapter.save(savedOffer);

        Optional<Offer> updatedOffer = databaseAdapter.findById(offerId);
        assertThat(updatedOffer.get().state()).isEqualTo(OfferState.PUBLISHED);
    }

    @Test
    void shouldPreventConcurrentModificationsWithOptimisticLocking() {
        // We have an offer
        OfferId offerId = new OfferId("OFF-DB-3");
        Offer baseOffer = new Offer(offerId, new CustomerId("C-3"), new SpecificationId("S-3"), Money.of(100000, "PLN"));
        databaseAdapter.save(baseOffer);

        // Two separate threads load the same data
        Offer viewA = databaseAdapter.findById(offerId).orElseThrow();
        Offer viewB = databaseAdapter.findById(offerId).orElseThrow();

        // Thread A saves the changes (publishes the offer)
        viewA.publishOffer();
        databaseAdapter.save(viewA);

        // Thread B rejects and tries to save
        viewB.reject();

        // The database rejects thread B's save due to a stale object version (Optimistic Lock)
        assertThatThrownBy(() -> databaseAdapter.save(viewB))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}