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
 * Adapter wyjściowy (ExternalApiAdapter, ACL) — klient HTTP modułu Katalogu (UC-CRM-02).
 * Realizuje port Sprzedaży {@link CatalogPriceQueryPort}: tłumaczy JSON z zewnętrznego
 * API na hermetyczny obiekt wartości Money (salon.common.model.Money).
 *
 * Zależność prowadzi do wewnątrz: adapter zależy od portu zdefiniowanego przez własny
 * kontekst (Sprzedaż), a nie od portu repozytorium Katalogu.
 *
 * HTTP 404 -> SpecificationNotFoundException (kontrolowany błąd biznesowy),
 * HTTP 5xx/timeout -> ExternalServiceUnavailableException (błąd infrastruktury).
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
    public Money getSpecificationPrice(String specificationId) {
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
