package salon.sales.infrastructure.out.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import salon.sales.application.port.out.InventoryIntegration;
import salon.sales.application.domain.exception.ExternalServiceUnavailableException;
import salon.sales.application.domain.exception.InventoryLockedException;

import java.util.Map;

/**
 * Outbound adapter (ExternalApiAdapter) of the {@link InventoryIntegration} port —
 * an HTTP client to the Inventory and Logistics Context.
 *
 * HTTP 409 (reservation conflict/no slots) -> InventoryLockedException with a reason from JSON,
 * HTTP 5xx/timeout -> ExternalServiceUnavailableException.
 */
@Component
public class InventoryExternalApiAdapter implements InventoryIntegration {

    private static final int TIMEOUT_MILLIS = 2000;

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public InventoryExternalApiAdapter(@Value("${wiremock.server.port:8080}") int inventoryPort) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MILLIS);
        factory.setReadTimeout(TIMEOUT_MILLIS);
        this.restTemplate = new RestTemplate(factory);
        this.baseUrl = "http://localhost:" + inventoryPort;
    }

    /** UC-CRM-03 -> UC-INW-01/02: reservation of a vehicle from the yard or a production slot. */
    @Override
    public void allocateVehicleOrProductionSlot(String orderId) {
        try {
            this.restTemplate.postForEntity(this.baseUrl + "/api/inventory/allocations",
                    Map.of("orderId", orderId), Void.class);
        } catch (HttpClientErrorException.Conflict e) {
            throw new InventoryLockedException(extractReason(e.getResponseBodyAsString()));
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new ExternalServiceUnavailableException(
                    "Inventory system is temporarily unavailable", e);
        }
    }

    /** UC-CRM-05, step 3: the ReleaseVehicle command (UC-INW-06). No VIN = no physical lock. */
    @Override
    public void releasePhysicalVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.isBlank()) {
            return; // the vehicle has not been assigned yet — there is nothing to release
        }
        try {
            this.restTemplate.postForEntity(this.baseUrl + "/api/inventory/releases",
                    Map.of("vehicleId", vehicleId), Void.class);
        } catch (HttpClientErrorException.Conflict e) {
            throw new InventoryLockedException(extractReason(e.getResponseBodyAsString()));
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new ExternalServiceUnavailableException(
                    "Inventory system is temporarily unavailable", e);
        }
    }

    // Extracts the "reason" field from a simple error JSON ({"reason": "..."}).
    private String extractReason(String body) {
        if (body != null) {
            int idx = body.indexOf("\"reason\"");
            if (idx >= 0) {
                int colon = body.indexOf(':', idx);
                int firstQuote = body.indexOf('"', colon + 1);
                int lastQuote = body.indexOf('"', firstQuote + 1);
                if (firstQuote >= 0 && lastQuote > firstQuote) {
                    return body.substring(firstQuote + 1, lastQuote);
                }
            }
        }
        return "Inventory rejected the operation";
    }
}
