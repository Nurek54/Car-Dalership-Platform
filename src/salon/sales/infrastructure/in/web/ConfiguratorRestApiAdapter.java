package salon.sales.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.service.ConfiguratorAppService;

/**
 * INBOUND ADAPTER (Figure 22: RestController) — opens a configurator session over HTTP (UC-CRM-01).
 */
@RestController
@RequestMapping("/api/sales/sessions")
public class ConfiguratorRestApiAdapter {

    private final ConfiguratorAppService configuratorAppService;

    public ConfiguratorRestApiAdapter(ConfiguratorAppService configuratorAppService) {
        this.configuratorAppService = configuratorAppService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<String> initiate(@RequestBody StartSessionRequest request) {
        String sessionId = this.configuratorAppService.startConfiguratorSession(
                new StartConfiguratorSessionCommand(request.customerId(), request.salespersonId()));
        return ResponseEntity.ok(sessionId);
    }

    /** Request body for starting a configurator session. */
    public record StartSessionRequest(String customerId, String salespersonId) {
    }
}
