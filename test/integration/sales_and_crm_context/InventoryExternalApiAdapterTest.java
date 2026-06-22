package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import salon.catalog.infrastructure.out.messaging.MessageBroker;
import salon.sales.infrastructure.out.external.InventoryExternalApiAdapter;
import salon.sales.application.domain.exception.InventoryLockedException;
import salon.sales.application.domain.exception.ExternalServiceUnavailableException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = InventoryExternalApiAdapter.class)
@AutoConfigureWireMock(port = 8082)
class InventoryExternalApiAdapterTest {

    @Autowired private InventoryExternalApiAdapter inventoryAdapter;

    @Test
    void shouldSuccessfullyAllocateVehicleSlotOverHttp() {
        // The Inventory system accepts the reservation and responds 201 Created
        stubFor(post(urlEqualTo("/api/inventory/allocations"))
                .withRequestBody(matchingJsonPath("$.orderId", equalTo("ORD-111")))
                .willReturn(aResponse().withStatus(201)));

        // The query passes without exceptions
        assertDoesNotThrow(() -> inventoryAdapter.allocateVehicleOrProductionSlot("ORD-111"));

        // We make sure there was actually a POST
        verify(1, postRequestedFor(urlPathEqualTo("/api/inventory/allocations")));
    }

    @Test
    void shouldThrowDomainExceptionWhenInventoryRejectsAllocationWith409() {
        // Inventory responds 409 Conflict (e.g. no parts to produce this model)
        stubFor(post(urlEqualTo("/api/inventory/allocations"))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withBody("{\"reason\": \"No production slots available for this specification\"}")));

        // The HTTP adapter correctly parses the error and throws a safe domain exception
        assertThatThrownBy(() -> inventoryAdapter.allocateVehicleOrProductionSlot("ORD-222"))
                .isInstanceOf(InventoryLockedException.class)
                .hasMessageContaining("Inventory rejected the allocation (409 Conflict)");
    }

    @Test
    void shouldThrowInfrastructureExceptionWhenInventoryIsDown() {
        // The logistics server is down and returns 503 Service Unavailable
        stubFor(post(urlEqualTo("/api/inventory/allocations"))
                .willReturn(aResponse().withStatus(503)));

        // The adapter throws an unavailability notice
        assertThatThrownBy(() -> inventoryAdapter.allocateVehicleOrProductionSlot("ORD-333"))
                .isInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessageContaining("Inventory system is temporarily unavailable");
    }
}