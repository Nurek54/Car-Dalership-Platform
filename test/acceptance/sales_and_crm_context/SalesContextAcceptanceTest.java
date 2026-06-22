package acceptance.sales_and_crm_context;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.web.servlet.MockMvc;

import salon.sales.application.domain.event.*;
import salon.sales.application.domain.model.customer.Address;
import salon.sales.application.domain.model.customer.ContactData;
import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.offer.OfferState;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.domain.model.order.OrderState;
import salon.sales.application.port.out.CustomerDatabaseRepository;
import salon.sales.infrastructure.out.persistence.OfferDatabaseAdapter;
import salon.sales.infrastructure.out.persistence.OrderDatabaseAdapter;
import salon.common.model.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "sales.catalog.base-url=http://localhost:8089",
        "sales.inventory.base-url=http://localhost:8089",
        "sales.billing.base-url=http://localhost:8089"
})
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8089) // We fake the rest of the company (Catalog, Inventory, Accounting) on a single port
class SalesContextAcceptanceTest {

    @Autowired private MockMvc mockMvc; // Replaces the browser / front-end
    @Autowired private ObjectMapper objectMapper; // For generating JSON

    @Autowired private OfferDatabaseAdapter offerRepository;
    @Autowired private OrderDatabaseAdapter orderRepository;
    @Autowired private CustomerDatabaseRepository customerRepository;

    @MockBean private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setupWireMock() {
        // We reset the fake servers before each use case
        resetAllRequests();
    }

    // ===================================================================================
    // UC-CRM-01: Initiation of a configurator session by the Salesperson
    // ===================================================================================
    @Test
    void uc01_shouldInitiateConfiguratorSession() throws Exception {
        // The customer must already exist in the CRM master data
        customerRepository.save(new Customer(new CustomerId("CUST-001"), "Jan Kowalski", "1234563218",
                new Address("Testowa 1", "00-001", "Warszawa", "Poland"),
                new ContactData("jan@example.com", "123456789")));

        // The Salesperson wants to open the configurator for a specific customer
        String payload = """
                {
                    "customerId": "CUST-001",
                    "salespersonId": "SALES-007"
                }
                """;

        mockMvc.perform(post("/api/sales/sessions/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // We verify that the system emitted the correct asynchronous event onto the queue
        verify(rabbitTemplate).convertAndSend(                    // By default once. JAVA to JSON
                eq("sales.events.exchange"),                // Where it sends
                eq("configurator.session.initiated"),       // Etykieta
                any(Object.class)                                 // The message body
        );
    }

    // ===================================================================================
    // UC-CRM-02 & UC-SPR-02: Acceptance of a Published Offer and order creation
    // ===================================================================================
    @Test
    void uc02_shouldAcceptPublishedOfferAndPlaceOrder() throws Exception {
        // We have a prepared and published offer for the customer in the database
        OfferId offerId = new OfferId("OFF-555");
        Offer offer = new Offer(offerId, new salon.sales.application.domain.model.customer.CustomerId("CUST-1"),
                new SpecificationId("SPEC-1"), Money.of(200000, "PLN"));
        offer.publishOffer();
        offerRepository.save(offer);

        // The customer clicks "Accept" on the page
        mockMvc.perform(post("/api/sales/offers/OFF-555/accept")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // The offer state in the database must change to ACCEPTED
        Offer savedOffer = offerRepository.findById(offerId).orElseThrow();
        assertThat(savedOffer.state()).isEqualTo(OfferState.ACCEPTED);

        // A new order must have been created in the database!
        Order newlyCreatedOrder = orderRepository.findByOfferId(offerId).orElseThrow();
        assertThat(newlyCreatedOrder.state()).isEqualTo(OrderState.DRAFT_CREATED);

        // The domain sent an order-placed Event to RabbitMQ
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("order.placed"), any(Object.class)
        );
    }

    // ===================================================================================
    // UC-CRM-03: Order Cancellation (the Customer withdraws)
    // ===================================================================================
    @Test
    void uc03_shouldCancelOrderAndReleaseInventory() throws Exception {
        // There is an order in the database
        OrderId orderId = new OrderId("ORD-777");
        Order order = new Order(orderId, new OfferId("OFF-777"), Money.of(100000, "PLN"));
        orderRepository.save(order);

        String payload = "{ \"reason\": \"Loss of financial capacity\" }";

        // The Salesperson clicks "Cancel" in the CRM system
        mockMvc.perform(post("/api/sales/orders/ORD-777/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // The aggregate in the database has the CANCELLED status
        Order cancelledOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(cancelledOrder.state()).isEqualTo(OrderState.CANCELLED);

        // We notified the external systems about the cancellation with the appropriate reason
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("order.cancelled"), any(Object.class)
        );
    }

    // ===================================================================================
    // UC-CRM-04: Notification of readiness for handover (External Event -> CRM)
    // ===================================================================================
    @Test
    void uc04_shouldMarkOrderAsReadyWhenVehicleArrivesFromLogistics() throws Exception {
        // We have an active order in production
        OrderId orderId = new OrderId("ORD-888");
        Order order = new Order(orderId, new OfferId("OFF-888"), Money.of(250000, "PLN"));
        order.activate(); // State: IN_PROGRESS
        orderRepository.save(order);

        // From outside the system (Inventory) an Event arrives that the physical car has come into the yard
        String incomingEventJson = """
                {
                    "eventId": "12312312-1231-1231-1231-123123123123",
                    "vin": "WBA123456789",
                    "orderId": "ORD-888",
                    "occurredOn": "2026-06-11T12:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/sales/webhooks/inventory/vehicle-ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incomingEventJson))
                .andExpect(status().isOk());

        // The order state in the Aggregate changed to READY_FOR_HANDOVER
        Order readyOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(readyOrder.state()).isEqualTo(OrderState.READY_FOR_HANDOVER);

        // The sales module in turn releases its own Event
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("order.ready_for_handover"), any(Object.class)
        );
    }

    // ===================================================================================
    // UC-SPR-08: Physical vehicle handover and closing the accounting
    // ===================================================================================
    @Test
    void uc05_shouldHandoverVehicleAndNotifyBilling() throws Exception {
        // The car is waiting in the yard, ready for handover
        OrderId orderId = new OrderId("ORD-999");
        Order order = new Order(orderId, new OfferId("OFF-999"), Money.of(300000, "PLN"));
        order.activate();
        order.markAsReadyForHandover(); // State: READY_FOR_HANDOVER
        orderRepository.save(order);

        // Inventory accepts the physical-release command
        stubFor(WireMock.post(urlEqualTo("/api/inventory/releases"))
                .willReturn(aResponse().withStatus(200)));

        // The Salesperson hands over the keys and clicks "Handed over" in the system
        mockMvc.perform(post("/api/sales/orders/ORD-999/handover")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // The order state becomes COMPLETED
        Order completedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(completedOrder.state()).isEqualTo(OrderState.COMPLETED);

        // An Event for the Billing / After-sales modules
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("vehicle.handed_over"), any(Object.class)
        );
    }
}
