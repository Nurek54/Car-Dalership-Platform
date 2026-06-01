package salon.sales.application.port.in;

import java.math.BigDecimal;

/**
 * Komenda dla UC-SPR-01. discountPercentage może być null (oferta bez rabatu).
 * basePrice opcjonalna — pozwala policzyć cenę końcową.
 */
public record CreateOfferCommand(String customerId,
                                 String specificationId,
                                 BigDecimal basePrice,
                                 String currency,
                                 BigDecimal discountPercentage,
                                 BigDecimal salespersonDiscountLimit) {

    public CreateOfferCommand {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank.");
        }
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId must not be blank.");
        }
    }
}
