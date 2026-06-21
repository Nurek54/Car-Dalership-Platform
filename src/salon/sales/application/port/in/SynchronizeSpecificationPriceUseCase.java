package salon.sales.application.port.in;

import salon.common.model.Money;

/**
 * Inbound port (driving) — synchronization of the local specification pricing in the
 * Sales Context based on the SpecificationCompleted event from the Catalog (UC-CRM-02,
 * a precondition). Invoked by the event-subscriber adapter (CatalogEventSubscriberAdapter).
 */
public interface SynchronizeSpecificationPriceUseCase {

    /** Saving the catalog pricing of the specification (from the SpecificationCompleted event). */
    void registerSpecificationPrice(String specificationId, Money price);
}
