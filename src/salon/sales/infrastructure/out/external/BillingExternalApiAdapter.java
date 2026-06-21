package salon.sales.infrastructure.out.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import salon.sales.application.port.out.BillingIntegration;
import salon.sales.application.domain.exception.ExternalServiceUnavailableException;
import salon.common.model.Money;

import java.util.Map;

/**
 * Outbound adapter (ExternalApiAdapter) of the {@link BillingIntegration} port —
 * an HTTP client to the Billing and Settlement module (PDF chapter 3.1.1).
 *
 * A short timeout (2 s) protects the sales process from a hung accounting system:
 * po jego przekroczeniu adapter rzuca ExternalServiceUnavailableException.
 */
@Component
public class BillingExternalApiAdapter implements BillingIntegration {

    private static final int TIMEOUT_MILLIS = 2000;

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public BillingExternalApiAdapter(@Value("${wiremock.server.port:8080}") int billingPort) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MILLIS);
        factory.setReadTimeout(TIMEOUT_MILLIS);
        this.restTemplate = new RestTemplate(factory);
        this.baseUrl = "http://localhost:" + billingPort;
    }

    @Override
    public void requestProformaInvoice(String orderId, Money amount) {
        try {
            Map<String, String> payload = Map.of(
                    "orderId", orderId,
                    "amount", amount.amount().setScale(2).toPlainString());
            this.restTemplate.postForEntity(this.baseUrl + "/api/billing/proforma-requests",
                    payload, Void.class);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceUnavailableException(
                    "Billing service is currently unavailable", e);
        }
    }

    @Override
    public void processCancelledOrderBilling(String orderId, String reason) {
        try {
            Map<String, String> payload = Map.of("orderId", orderId, "reason", reason);
            this.restTemplate.postForEntity(this.baseUrl + "/api/billing/cancellations",
                    payload, Void.class);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceUnavailableException(
                    "Billing service is currently unavailable", e);
        }
    }

    @Override
    public void closeOrderBalance(String orderId) {
        try {
            this.restTemplate.put(this.baseUrl + "/api/billing/accounts/" + orderId + "/close", null);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceUnavailableException(
                    "Billing service is currently unavailable", e);
        }
    }
}
