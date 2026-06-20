package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import salon.sales.infrastructure.out.external.InventoryExternalApiAdapter;
import salon.sales.application.domain.exception.InventoryLockedException;
import salon.sales.application.domain.exception.ExternalServiceUnavailableException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@AutoConfigureWireMock(port = 8082)
class InventoryExternalApiAdapterTest {

    @Autowired private InventoryExternalApiAdapter inventoryAdapter;

    @Test
    void shouldSuccessfullyAllocateVehicleSlotOverHttp() {
        // System Inwentarza przyjmuje rezerwację i odpowiada 201 Created
        stubFor(post(urlEqualTo("/api/inventory/allocations"))
                .withRequestBody(matchingJsonPath("$.orderId", equalTo("ORD-111")))
                .willReturn(aResponse().withStatus(201)));

        // Zapytanie przechodzi bez wyjątków
        assertDoesNotThrow(() -> inventoryAdapter.allocateVehicleOrProductionSlot("ORD-111"));

        // Upewniamy się, że faktycznie był POST
        verify(1, postRequestedFor(urlPathEqualTo("/api/inventory/allocations")));
    }

    @Test
    void shouldThrowDomainExceptionWhenInventoryRejectsAllocationWith409() {
        // Inwentarz odpowiada 409 Conflict (np. brak części do produkcji tego modelu)
        stubFor(post(urlEqualTo("/api/inventory/allocations"))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withBody("{\"reason\": \"No production slots available for this specification\"}")));

        // Adapter HTTP poprawnie parsuje błąd i rzuca bezpieczny wyjątek dziedziny
        assertThatThrownBy(() -> inventoryAdapter.allocateVehicleOrProductionSlot("ORD-222"))
                .isInstanceOf(InventoryLockedException.class)
                .hasMessageContaining("No production slots available");
    }

    @Test
    void shouldThrowInfrastructureExceptionWhenInventoryIsDown() {
        // Serwer logistyki nie działa i zwraca 503 Service Unavailable
        stubFor(post(urlEqualTo("/api/inventory/allocations"))
                .willReturn(aResponse().withStatus(503)));

        // Adapter rzuca informację o braku usługi
        assertThatThrownBy(() -> inventoryAdapter.allocateVehicleOrProductionSlot("ORD-333"))
                .isInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessageContaining("Inventory system is temporarily unavailable");
    }
}