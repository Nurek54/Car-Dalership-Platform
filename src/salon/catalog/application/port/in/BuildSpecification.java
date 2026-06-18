package salon.catalog.application.port.in;

import salon.common.model.SpecificationId;

/**
 * Port wejsciowy (driving) dla UC-KON-01 (konfiguracja pojazdu), zgodnie z diagramem (Rys. 28).
 * Realizowany przez BuildSpecificationService.
 */
public interface BuildSpecification {
    SpecificationId startSpecification(String catalogId);
    void addOption(String specificationId, String catalogId, String optionCode);
    void finalizeSpecification(String specificationId);
}
