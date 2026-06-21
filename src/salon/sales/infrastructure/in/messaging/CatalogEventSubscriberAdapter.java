package salon.sales.infrastructure.in.messaging;

import salon.catalog.application.domain.model.event.CatalogUpdated;
import salon.catalog.application.domain.model.event.SpecificationCompleted;
import salon.common.model.Money;
import salon.sales.application.port.in.SynchronizeSpecificationPriceUseCase;

/**
 * Driving adapter — subscriber of the Catalog and Configurator Context events
 * in the Sales Context (communication per the canvas: SpecificationCompleted, CatalogUpdated).
 *
 * Anti-corruption layer (ACL): the adapter receives events in the Catalog language
 * (the Catalog's SpecificationId/Money/CatalogId) and TRANSLATES them into the Sales model
 * (String + salon.common.model.Money) before invoking the inbound port. Thanks to this
 * the Sales domain does not depend on the Catalog model.
 *
 * UC-CRM-02, precondition / step 1: after receiving SpecificationCompleted the adapter saves
 * the computed catalog price into the local read model (event-carried state transfer) and notifies
 * the Salesperson about a new specification awaiting offering.
 */
public class CatalogEventSubscriberAdapter {

    private final SynchronizeSpecificationPriceUseCase synchronizeSpecificationPrice;

    public CatalogEventSubscriberAdapter(SynchronizeSpecificationPriceUseCase synchronizeSpecificationPrice) {
        if (synchronizeSpecificationPrice == null) {
            throw new IllegalArgumentException("synchronizeSpecificationPrice must not be null.");
        }
        this.synchronizeSpecificationPrice = synchronizeSpecificationPrice;
    }

    /** UC-CRM-02, step 1: a new complete specification -> saving the pricing + notifying the Salesperson. */
    public void handleSpecificationCompleted(SpecificationCompleted event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        // Translation from the Catalog language into the Sales language (ACL).
        String specificationId = event.specificationId().toString();
        Money price = Money.of(
                event.totalPrice().amount(),
                event.totalPrice().currency().getCurrencyCode());

        this.synchronizeSpecificationPrice.registerSpecificationPrice(specificationId, price);
        System.out.println("[CatalogEventSubscriberAdapter] Specification " + specificationId
                + " ready for offering (price " + price + ") — notifying the Salesperson.");
    }

    /** UC-KON-02: a new price list version was published — information for the sales team. */
    public void handleCatalogUpdated(CatalogUpdated event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        String catalogId = event.catalogId().toString();
        System.out.println("[CatalogEventSubscriberAdapter] New price list version " + catalogId
                + " — new offers will be built on the updated catalog.");
    }
}
