package salon.logistics.application.domain.model.vehicle;

import java.util.List;

public record ImporterData(VinNumber vin, SpecificationId specificationId, List<String> optionCodes) {

    public ImporterData {
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        optionCodes = optionCodes == null ? List.of() : List.copyOf(optionCodes);
    }
}
