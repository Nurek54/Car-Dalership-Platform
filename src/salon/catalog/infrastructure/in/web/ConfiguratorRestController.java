package salon.catalog.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.catalog.application.port.in.BuildSpecification;
import salon.common.model.SpecificationId;

import java.util.Map;

/**
 * Adapter sterujacy (driving) — REST API konfiguratora pojazdu (UC-KON-01).
 * Cienki: deleguje do portu wejsciowego BuildSpecification.
 */
@RestController
@RequestMapping("/api/catalog/specifications")
public class ConfiguratorRestController {

    private final BuildSpecification buildSpecification;

    public ConfiguratorRestController(BuildSpecification buildSpecification) {
        if (buildSpecification == null) {
            throw new IllegalArgumentException("buildSpecification must not be null.");
        }
        this.buildSpecification = buildSpecification;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> start(@RequestBody StartRequest request) {
        SpecificationId id = buildSpecification.startSpecification(request.catalogId());
        return ResponseEntity.ok(Map.of("specificationId", id.value()));
    }

    @PostMapping("/{specificationId}/options")
    public ResponseEntity<Void> pickOption(@PathVariable String specificationId,
                                           @RequestBody PickOptionRequest request) {
        buildSpecification.addOption(specificationId, request.catalogId(), request.optionCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{specificationId}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable String specificationId) {
        buildSpecification.finalizeSpecification(specificationId);
        return ResponseEntity.ok().build();
    }

    public record StartRequest(String catalogId) {}

    public record PickOptionRequest(String catalogId, String optionCode) {}
}
