package salon.logistics.application.domain.model.vehicle;

import java.util.List;

/**
 * Obiekt wartości (Rysunek 38) – dane pojazdu dostarczone przez system importera/fabryki
 * w momencie zjazdu z lawety (UC-INW-03). Zgodnie z założeniem kanwy skan numeru VIN jest
 * bezbłędny i pokrywa się z cyfrowymi danymi zamówienia produkcyjnego, dlatego to właśnie
 * {@code ImporterData} jest źródłem prawdy przy przyjęciu pojazdu na plac.
 */
public record ImporterData(VinNumber vin, SpecificationId specificationId, List<String> optionCodes) {

    public ImporterData {
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        optionCodes = optionCodes == null ? List.of() : List.copyOf(optionCodes);
    }
}
