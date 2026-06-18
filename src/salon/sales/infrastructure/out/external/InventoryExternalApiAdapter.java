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
 * Adapter wyjściowy (ExternalApiAdapter) portu {@link InventoryIntegration} —
 * klient HTTP do Kontekstu Inwentarza i Logistyki.
 *
 * HTTP 409 (konflikt rezerwacji/brak slotów) -> InventoryLockedException z powodem z JSON,
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

    /** UC-CRM-03 -> UC-INW-01/02: rezerwacja pojazdu z placu lub slotu produkcyjnego. */
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

    /** UC-CRM-05, krok 3: komenda ReleaseVehicle (UC-INW-06). Brak VIN = brak fizycznej blokady. */
    @Override
    public void releasePhysicalVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.isBlank()) {
            return; // pojazd nie został jeszcze przypisany — nie ma czego zwalniać
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

    // Wyciąga pole "reason" z prostego JSON-a błędu ({"reason": "..."}).
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
