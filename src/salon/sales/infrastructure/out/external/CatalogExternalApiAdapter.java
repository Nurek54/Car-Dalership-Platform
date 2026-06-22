package salon.sales.infrastructure.out.external;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import salon.common.model.Money;
import salon.sales.application.domain.exception.ExternalServiceUnavailableException;
import salon.sales.application.domain.exception.SpecificationNotFoundException;

import java.math.BigDecimal;

/**
 * OUTBOUND ADAPTER (ACL) — HTTP integration with the Catalog and Configuration Context.
 * Translates the external JSON price into the {@link Money} value object and maps transport errors
 * (404/5xx/timeout) onto controlled domain exceptions.
 */
@Component
public class CatalogExternalApiAdapter {

    private final RestClient restClient;

    public CatalogExternalApiAdapter(@Value("${sales.catalog.base-url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** Fetches the configured specification price from the Catalog. */
    public Money specificationPrice(String specificationId) {
        try {
            ResponseEntity<JsonNode> response = this.restClient.get()
                    .uri("/api/catalog/specifications/{id}/price", specificationId)
                    .retrieve()
                    .toEntity(JsonNode.class);
            JsonNode body = response.getBody();
            BigDecimal amount = new BigDecimal(body.get("amount").asText());
            String currency = body.get("currency").asText();
            return Money.of(amount, currency);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new SpecificationNotFoundException("Specification " + specificationId + " not found in Catalog");
        } catch (HttpServerErrorException | ResourceAccessException ex) {
            throw new ExternalServiceUnavailableException("Catalog service is currently unavailable", ex);
        }
    }
}
