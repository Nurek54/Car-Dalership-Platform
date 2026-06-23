package salon.sales.infrastructure.out.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import salon.common.model.Money;
import salon.sales.application.domain.exception.ExternalServiceUnavailableException;

import java.util.Map;

@Component
public class BillingExternalApiAdapter {

    private final RestClient restClient;

    public BillingExternalApiAdapter(@Value("${sales.billing.base-url:http://localhost:8083}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(2000);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    
    public void requestProformaInvoice(String orderId, Money amount) {
        String formattedAmount = amount.amount().setScale(2).toPlainString();
        try {
            this.restClient.post()
                    .uri("/api/billing/proforma-requests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("orderId", orderId, "amount", formattedAmount))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpServerErrorException | ResourceAccessException ex) {
            throw new ExternalServiceUnavailableException("Billing service is currently unavailable", ex);
        }
    }

    
    public void closeOrderBalance(String orderId) {
        try {
            this.restClient.put()
                    .uri("/api/billing/accounts/{orderId}/close", orderId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpServerErrorException | ResourceAccessException ex) {
            throw new ExternalServiceUnavailableException("Billing service is currently unavailable", ex);
        }
    }
}
