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
        // Księgowość przyjmuje zlecenie na proformę (202 Accepted)
        Money deposit = Money.of(50000, "PLN");
        stubFor(post(urlEqualTo("/api/billing/proforma-requests"))
                .withRequestBody(matchingJsonPath("$.orderId", equalTo("ORD-888")))
                .withRequestBody(matchingJsonPath("$.amount", equalTo("50000.00")))
                .willReturn(aResponse().withStatus(202)));

        // Metoda wykonuje się prawidłowo, powiadamiając system księgowy
        assertDoesNotThrow(() -> billingAdapter.requestProformaInvoice("ORD-888", deposit));
    }

    @Test
    void shouldCloseOrderBalanceSuccessfully() {
        // Zgłaszamy wydanie auta, Księgowość domyka saldo końcowe i wystawia fakturę VAT
        stubFor(put(urlEqualTo("/api/billing/accounts/ORD-999/close"))
                .willReturn(aResponse().withStatus(200)));

        // Adapter pomyślnie wysyła żądanie typu PUT (aktualizacja stanu)
        assertDoesNotThrow(() -> billingAdapter.closeOrderBalance("ORD-999"));

        // Weryfikacja
        verify(1, putRequestedFor(urlEqualTo("/api/billing/accounts/ORD-999/close")));
    }

    @Test
    void shouldThrowExceptionWhenBillingSystemTimesOut() {
        // System księgowy zawiesił się
        stubFor(post(urlEqualTo("/api/billing/proforma-requests"))
                .willReturn(aResponse()
                        .withFixedDelay(5000) // WireMock wstrzyma odpowiedź o 5 sekund
                        .withStatus(200)));

        // Nasz adapter (który powinien mieć skonfigurowany np. 2-sekundowy timeout)
        // przerywa oczekiwanie i rzuca własnym wyjątkiem o niedostępności
        Money deposit = Money.of(10000, "PLN");
        assertThatThrownBy(() -> billingAdapter.requestProformaInvoice("ORD-TIMEOUT", deposit))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }
}