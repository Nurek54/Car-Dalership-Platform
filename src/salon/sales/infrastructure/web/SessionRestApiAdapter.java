package salon.sales.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.service.SalesAppService;

import java.util.Map;

/**
 * Adapter sterujący (driving) — REST API sesji konfiguratora (UC-CRM-01).
 * Handlowiec inicjuje sesję dla klienta; kontekst emituje InitiateConfiguratorSession.
 */
@RestController
@RequestMapping("/api/sales/sessions")
public class SessionRestApiAdapter {

    private final SalesAppService salesAppService;

    public SessionRestApiAdapter(SalesAppService salesAppService) {
        if (salesAppService == null) {
            throw new IllegalArgumentException("salesAppService must not be null.");
        }
        this.salesAppService = salesAppService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<Map<String, String>> initiateSession(
            @RequestBody InitiateSessionRequest request) {
        String sessionId = salesAppService.startConfiguratorSession(
                new StartConfiguratorSessionCommand(request.customerId(), request.salespersonId()));
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }

    /** DTO wejściowe UC-CRM-01. */
    public record InitiateSessionRequest(String customerId, String salespersonId) {
    }
}
