package salon.sales.infrastructure.in.messaging;

import salon.common.model.Money;
import salon.sales.application.service.SalesService;

import java.math.BigDecimal;

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

    public record SpecificationCompleted(String customerId, String specificationId,
                                         BigDecimal basePrice, String currency) {
    }
}
