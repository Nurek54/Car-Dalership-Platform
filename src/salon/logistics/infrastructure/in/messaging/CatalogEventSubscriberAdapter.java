package salon.logistics.infrastructure.in.messaging;

import salon.logistics.application.port.out.CatalogIntegration;

import java.util.List;

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

    public record SpecificationCompleted(String specificationId, List<String> optionCodes) {
    }
}
