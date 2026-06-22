package salon.sales.application.port.out;

import salon.common.model.Money;
import salon.common.model.SpecificationId;

import java.util.Optional;

/**
 * OUTBOUND PORT — local read model of specification pricing, fed asynchronously by the Catalog's
 * SpecificationCompleted event (UC-CRM-02). Lets Sales build an offer without a synchronous query
 * to the Catalog Context.
 */
public interface SpecificationPriceReadModelPort {

    Optional<Money> findPrice(SpecificationId specificationId);

    /** Stores/updates the price for a specification (called by the inbound Catalog event adapter). */
    void savePrice(SpecificationId specificationId, Money price);
}
