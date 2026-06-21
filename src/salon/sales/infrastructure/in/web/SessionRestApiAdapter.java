package salon.sales.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.service.SalesService;

import java.util.Map;

/**
 * Driving adapter — REST API for configurator sessions (UC-CRM-01).
 * The Salesperson initiates a session for the customer; the context emits InitiateConfiguratorSession.
 */
@RestController
@RequestMapping("/api/sales/sessions")
public class SessionRestApiAdapter {

    private final SalesService salesAppService;

    public SessionRestApiAdapter(SalesService salesAppService) {
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

    /** Input DTO for UC-CRM-01. */
    public record InitiateSessionRequest(String customerId, String salespersonId) {
    }
}
