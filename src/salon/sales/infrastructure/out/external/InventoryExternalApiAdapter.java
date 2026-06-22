package salon.sales.infrastructure.out.external;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import salon.sales.application.domain.exception.ExternalServiceUnavailableException;
import salon.sales.application.domain.exception.InventoryLockedException;
import salon.sales.application.port.out.InventoryIntegration;

import java.util.Map;

/**
 * OUTBOUND ADAPTER (ACL) — HTTP integration with the Inventory and Logistics Context.
 * Allocates a vehicle/production slot and releases the physical vehicle; maps 409/5xx onto
 * controlled domain exceptions.
 */
@Component
public class InventoryExternalApiAdapter implements InventoryIntegration {

    private final RestClient restClient;

    public InventoryExternalApiAdapter(@Value("${sales.inventory.base-url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void allocateVehicleOrProductionSlot(String orderId) {
        try {
            this.restClient.post()
                    .uri("/api/inventory/allocations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("orderId", orderId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict ex) {
            throw new InventoryLockedException(extractReason(ex));
        } catch (HttpServerErrorException | ResourceAccessException ex) {
            throw new ExternalServiceUnavailableException("Inventory system is temporarily unavailable", ex);
        }
    }

    @Override
    public void releasePhysicalVehicle(String orderId) {
        try {
            this.restClient.post()
                    .uri("/api/inventory/releases")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("orderId", orderId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpServerErrorException | ResourceAccessException ex) {
            throw new ExternalServiceUnavailableException("Inventory system is temporarily unavailable", ex);
        }
    }

    @Override
    public void releaseVehicle(String orderId) {
        releasePhysicalVehicle(orderId);
    }

    private String extractReason(HttpClientErrorException ex) {
        try {
            JsonNode body = ex.getResponseBodyAs(JsonNode.class);
            if (body != null && body.has("reason")) {
                return body.get("reason").asText();
            }
        } catch (Exception ignored) {
            // fall through to a generic message
        }
        return "Inventory rejected the allocation (409 Conflict)";
    }
}
