package salon.sales.infrastructure.out.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import salon.sales.application.port.out.CatalogPriceQueryPort;
import salon.sales.application.domain.exception.ExternalServiceUnavailableException;
import salon.sales.application.domain.exception.SpecificationNotFoundException;
import salon.common.model.Money;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Outbound adapter (ExternalApiAdapter, ACL) — an HTTP client of the Catalog module (UC-CRM-02).
 * Implements the Sales port {@link CatalogPriceQueryPort}: it translates JSON from the external
 * API into the encapsulated value object Money (salon.common.model.Money).
 *
 * The dependency points inward: the adapter depends on a port defined by its own
 * context (Sales), not on the Catalog repository port.
 *
 * HTTP 404 -> SpecificationNotFoundException (a controlled business error),
 * HTTP 5xx/timeout -> ExternalServiceUnavailableException (an infrastructure error).
 */
@Component
public class CatalogExternalApiAdapter implements CatalogPriceQueryPort {

    private static final int TIMEOUT_MILLIS = 2000;

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CatalogExternalApiAdapter(@Value("${wiremock.server.port:8080}") int catalogPort) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MILLIS);
        factory.setReadTimeout(TIMEOUT_MILLIS);
        this.restTemplate = new RestTemplate(factory);
        this.baseUrl = "http://localhost:" + catalogPort;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Money specificationPrice(String specificationId) {
        try {
            Map<String, Object> body = this.restTemplate.getForObject(
                    this.baseUrl + "/api/catalog/specifications/" + specificationId + "/price",
                    Map.class);
            if (body == null || body.get("amount") == null || body.get("currency") == null) {
                throw new ExternalServiceUnavailableException(
                        "Catalog service returned an incomplete price payload");
            }
            BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
            return new Money(amount, String.valueOf(body.get("currency")));
        } catch (HttpClientErrorException.NotFound e) {
            throw new SpecificationNotFoundException(
                    "Specification " + specificationId + " not found in Catalog");
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new ExternalServiceUnavailableException(
                    "Catalog service is currently unavailable", e);
        }
    }
}
