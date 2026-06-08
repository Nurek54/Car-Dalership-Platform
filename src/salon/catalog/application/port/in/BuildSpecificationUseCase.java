package salon.catalog.application.port.in;

import salon.shared.model.SpecificationId;

/**
 * Port wejściowy dla UC-KON-01 (konfiguracja pojazdu).
 */
public interface BuildSpecificationUseCase {
    SpecificationId startSpecification(String catalogId);
    void addOption(String specificationId, String catalogId, String optionCode);
    void finalizeSpecification(String specificationId);
}
