package salon.catalog.infrastructure.out.persistence;

import java.math.BigDecimal;
import java.util.List;

/**
 * Persistent-storage model for the VehicleSpecification aggregate (infrastructure DTO).
 */
public record VehicleSpecificationRecord(String id,
                                         String catalogId,
                                         BigDecimal totalPrice,
                                         String currency,
                                         String state,
                                         List<String> optionsPicked) {
}
