package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import salon.sales.infrastructure.out.external.CatalogExternalApiAdapter;
import salon.common.model.Money;
import salon.sales.application.domain.exception.ExternalServiceUnavailableException;
import salon.sales.application.domain.exception.SpecificationNotFoundException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureWireMock(port = 8081)
class CatalogExternalApiAdapterTest {

    @Autowired private CatalogExternalApiAdapter catalogAdapter;

    @Test
    void shouldFetchDataWhenCatalogReturns200Ok() {
        // Katalog żyje i ma odpowiedź
        stubFor(get(urlEqualTo("/api/catalog/specifications/SPEC-OK/price"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"amount\": 150000, \"currency\": \"PLN\"}")));

        // Adapter uderza przez HTTP
        Money price = catalogAdapter.specificationPrice("SPEC-OK");

        // JSON z zewnątrz zamienia się w hermetyczny Value Object
        assertThat(price.amount()).isEqualByComparingTo(java.math.BigDecimal.valueOf(150000));
        assertThat(price.currency()).isEqualTo("PLN");
    }

    @Test
    void shouldThrowDomainExceptionWhenSpecificationIsNotFound404() {
        // Handlowiec podał nieistniejącą specyfikację, Katalog zwraca HTTP 404
        stubFor(get(urlEqualTo("/api/catalog/specifications/SPEC-MISSING/price"))
                .willReturn(aResponse().withStatus(404)));

        // Zamiast brzydkiego błędu HTTP, wyrzucamy kontrolowany błąd biznesowy
        assertThatThrownBy(() -> catalogAdapter.specificationPrice("SPEC-MISSING"))
                .isInstanceOf(SpecificationNotFoundException.class)
                .hasMessageContaining("Specification SPEC-MISSING not found in Catalog");
    }

    @Test
    void shouldThrowInfrastructureExceptionWhenCatalogIsDown500() {
        // Baza danych modułu Katalogu nie działa (HTTP 500)
        stubFor(get(urlEqualTo("/api/catalog/specifications/SPEC-TIMEOUT/price"))
                .willReturn(aResponse().withStatus(500)));

        // Aplikacja musi zasygnalizować brak dostępu do zewnętrznej usługi
        assertThatThrownBy(() -> catalogAdapter.specificationPrice("SPEC-TIMEOUT"))
                .isInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessageContaining("Catalog service is currently unavailable");
    }
}