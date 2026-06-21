package salon.logistics.application.domain.model.vehicle;

import java.util.List;

/**
 * Value object (Figure 38) – vehicle data provided by the importer/factory system
 * at the moment it comes off the transporter (UC-INW-03). Per the canvas assumption the VIN scan is
 * error-free and matches the digital data of the production order, which is why
 * {@code ImporterData} is the source of truth when receiving the vehicle into the yard.
 */
public record ImporterData(VinNumber vin, SpecificationId specificationId, List<String> optionCodes) {

    public ImporterData {
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        optionCodes = optionCodes == null ? List.of() : List.copyOf(optionCodes);
    }
}
