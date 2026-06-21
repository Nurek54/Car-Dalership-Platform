package salon.catalog.application.dto;

import salon.catalog.application.domain.model.specification.VehicleSpecification;

import java.math.BigDecimal;
import java.util.List;

/**
 * Output data model (read DTO) returned by the application service.
 *
 * Per the PDF (chapter 4): the service returns MINIMAL output data – a recommended value
 * object / DTO, not an aggregate or entity. This prevents the domain model from leaking
 * to the inbound adapters. It belongs to the application layer (the inbound port contract),
 * alongside – not inside – the application.port package (ports must be interfaces).
 */
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
