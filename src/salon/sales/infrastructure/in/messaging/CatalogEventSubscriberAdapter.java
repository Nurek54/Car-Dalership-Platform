package salon.sales.infrastructure.in.messaging;

import salon.common.model.Money;
import salon.sales.application.service.SalesService;

import java.math.BigDecimal;

/**
 * INBOUND ADAPTER (Figure 22: EventListener) — subscriber of the Catalog and Configuration Context.
 *
 * On SpecificationCompleted (event-carried state transfer of the configured price) a proforma offer
 * is generated (UC-CRM-02). ACL: the Catalog message is translated into a simple local record.
 */
public class CatalogEventSubscriberAdapter {

    private final SalesService salesService;

    public CatalogEventSubscriberAdapter(SalesService salesService) {
        if (salesService == null) {
            throw new IllegalArgumentException("salesService must not be null.");
        }
        this.salesService = salesService;
    }

    public void handleSpecificationCompleted(SpecificationCompleted event) {
        if (event == null || event.customerId() == null || event.specificationId() == null
                || event.basePrice() == null || event.currency() == null) {
            throw new IllegalArgumentException("SpecificationCompleted fields must not be null.");
        }
        this.salesService.createProformaOffer(
                event.customerId(), event.specificationId(), Money.of(event.basePrice(), event.currency()));
    }

    /** Local (ACL) representation of the SpecificationCompleted event from the Catalog. */
    public record SpecificationCompleted(String customerId, String specificationId,
                                         BigDecimal basePrice, String currency) {
    }
}
