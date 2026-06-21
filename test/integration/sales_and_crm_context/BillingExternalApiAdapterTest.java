package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import salon.sales.infrastructure.out.external.BillingExternalApiAdapter;
import salon.common.model.Money;
import salon.sales.application.domain.exception.ExternalServiceUnavailableException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@AutoConfigureWireMock(port = 8083)
class BillingExternalApiAdapterTest {

    @Autowired private BillingExternalApiAdapter billingAdapter;

    @Test
    void shouldRequestProformaInvoiceSuccessfully() {
        // Accounting accepts the proforma order (202 Accepted)
        Money deposit = Money.of(50000, "PLN");
        stubFor(post(urlEqualTo("/api/billing/proforma-requests"))
                .withRequestBody(matchingJsonPath("$.orderId", equalTo("ORD-888")))
                .withRequestBody(matchingJsonPath("$.amount", equalTo("50000.00")))
                .willReturn(aResponse().withStatus(202)));

        // The method executes correctly, notifying the accounting system
        assertDoesNotThrow(() -> billingAdapter.requestProformaInvoice("ORD-888", deposit));
    }

    @Test
    void shouldCloseOrderBalanceSuccessfully() {
        // We report the car handover, Accounting closes the final balance and issues the VAT invoice
        stubFor(put(urlEqualTo("/api/billing/accounts/ORD-999/close"))
                .willReturn(aResponse().withStatus(200)));

        // The adapter successfully sends a PUT request (state update)
        assertDoesNotThrow(() -> billingAdapter.closeOrderBalance("ORD-999"));

        // Weryfikacja
        verify(1, putRequestedFor(urlEqualTo("/api/billing/accounts/ORD-999/close")));
    }

    @Test
    void shouldThrowExceptionWhenBillingSystemTimesOut() {
        // The accounting system hung
        stubFor(post(urlEqualTo("/api/billing/proforma-requests"))
                .willReturn(aResponse()
                        .withFixedDelay(5000) // WireMock will delay the response by 5 seconds
                        .withStatus(200)));

        // Our adapter (which should have e.g. a 2-second timeout configured)
        // aborts the wait and throws its own unavailability exception
        Money deposit = Money.of(10000, "PLN");
        assertThatThrownBy(() -> billingAdapter.requestProformaInvoice("ORD-TIMEOUT", deposit))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }
}