package salon.catalog.application.dto;

import salon.catalog.application.domain.model.specification.VehicleSpecification;

import java.math.BigDecimal;
import java.util.List;

public record SpecificationView(String specificationId,
                                String catalogId,
                                String state,
                                BigDecimal totalPrice,
                                String currency,
                                List<String> optionsPicked) {

    public static SpecificationView from(VehicleSpecification specification) {
        return new SpecificationView(
                specification.id().toString(),
                specification.catalogId().toString(),
                specification.state().name(),
                specification.totalPrice().amount(),
                specification.totalPrice().currency().getCurrencyCode(),
                specification.optionsPicked().stream().map(Object::toString).toList());
    }
}
