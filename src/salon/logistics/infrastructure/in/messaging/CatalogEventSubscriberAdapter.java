package salon.logistics.infrastructure.in.messaging;

import salon.logistics.application.port.out.CatalogIntegration;

import java.util.List;

/**
 * ADAPTER WEJŚCIOWY (Rysunek 37: EventListener) – subskrybent zdarzeń Kontekstu Katalogu.
 *
 * SpecificationCompleted niesie kody wyposażenia (event-carried state transfer): adapter zapisuje
 * je do lokalnej kopii danych Katalogu (port {@link CatalogIntegration}), dzięki czemu UC-INW-01/02
 * nie wymagają synchronicznego odpytywania Katalogu. ACL: typy Katalogu tłumaczone są na prosty
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

    /** Lokalna (ACL) reprezentacja zdarzenia SpecificationCompleted z Katalogu. */
    public record SpecificationCompleted(String specificationId, List<String> optionCodes) {
    }
}
