package salon.sales.application.port.out;

import salon.common.model.Money;
import salon.common.model.SpecificationId;

import java.util.Optional;

/**
 * Outbound port (driven) — the local copy of the specification pricing in the Sales Context
 * (read model, event-carried state transfer).
 *
 * It replaces synchronous querying of the Catalog (the former CatalogDatabaseRepository.getSpecificationPrice):
 * the computed catalog price arrives asynchronously in the SpecificationCompleted event
 * (Catalog). At the moment of generating the offer (UC-CRM-02) Sales reads only its own data —
 * no synchronous communication between contexts.
 */
public interface SpecificationPriceReadModelPort {

    /** Saving/updating the specification pricing (from the SpecificationCompleted event). */
    void saveSpecificationPrice(SpecificationId specificationId, Money price);

    /** The specification pricing (an empty Optional = the price has not yet arrived via an event). */
    Optional<Money> findPrice(SpecificationId specificationId);
}
