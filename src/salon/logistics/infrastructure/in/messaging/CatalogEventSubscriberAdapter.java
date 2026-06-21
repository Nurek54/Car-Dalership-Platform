package salon.logistics.infrastructure.in.messaging;

import salon.logistics.application.port.out.CatalogIntegration;

import java.util.List;

/**
 * INBOUND ADAPTER (Figure 37: EventListener) – subscriber of the Catalog Context events.
 *
 * SpecificationCompleted carries the equipment codes (event-carried state transfer): the adapter writes
 * them into the local copy of the Catalog data (the {@link CatalogIntegration} port), so that UC-INW-01/02
 * do not require synchronous querying of the Catalog. ACL: the Catalog types are translated into a simple
 * model (String, List&lt;String&gt;).
 */
public class CatalogEventSubscriberAdapter {

    private final CatalogIntegration catalogIntegration;

    public CatalogEventSubscriberAdapter(CatalogIntegration catalogIntegration) {
        if (catalogIntegration == null) {
            throw new IllegalArgumentException("catalogIntegration must not be null.");
        }
        this.catalogIntegration = catalogIntegration;
    }

    public void handleSpecificationCompleted(SpecificationCompleted event) {
        if (event == null || event.specificationId() == null || event.specificationId().isBlank()) {
            throw new IllegalArgumentException("specificationId must not be blank.");
        }
        List<String> optionCodes = event.optionCodes() == null ? List.of() : event.optionCodes();
        this.catalogIntegration.saveSpecification(event.specificationId(), optionCodes);
    }

    /** Local (ACL) representation of the SpecificationCompleted event from the Catalog. */
    public record SpecificationCompleted(String specificationId, List<String> optionCodes) {
    }
}
