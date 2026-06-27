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
@AutoConfigureWireMock(port = 8089) // Resztę firmy (Katalog, Magazyn, Księgowość) udajemy na jednym porcie
class SalesContextAcceptanceTest {

    @Autowired private MockMvc mockMvc; // Zastępuje przeglądarkę / front-end
    @Autowired private ObjectMapper objectMapper; // Do generowania JSON-a

    @Autowired private OfferDatabaseAdapter offerRepository;
    @Autowired private OrderDatabaseAdapter orderRepository;
    @Autowired private CustomerDatabaseRepository customerRepository;

    @MockBean private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setupWireMock() {
        // Resetujemy atrapy serwerów przed każdym przypadkiem użycia
        resetAllRequests();
    }

    // ===================================================================================
    // UC-CRM-01: Zainicjowanie sesji konfiguratora przez Sprzedawcę
    // ===================================================================================
    @Test
    void uc01_shouldInitiateConfiguratorSession() throws Exception {
        // Klient musi już istnieć w danych podstawowych CRM
        customerRepository.save(new Customer(new CustomerId("CUST-001"), "Jan Kowalski", "1234563218",
                new Address("Testowa 1", "00-001", "Warszawa", "Poland"),
                new ContactData("jan@example.com", "123456789")));

        // Sprzedawca chce otworzyć konfigurator dla konkretnego klienta
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

        // Sprawdzamy, że system wyemitował poprawne zdarzenie asynchroniczne na kolejkę
        verify(rabbitTemplate).convertAndSend(                    // Domyślnie raz. JAVA na JSON
                eq("sales.events.exchange"),                // Gdzie wysyła
                eq("configurator.session.initiated"),       // Etykieta
                any(Object.class)                                 // Treść wiadomości
        );
    }

    // ===================================================================================
    // UC-CRM-02 i UC-SPR-02: Akceptacja opublikowanej oferty i utworzenie zamówienia
    // ===================================================================================
    @Test
    void uc02_shouldAcceptPublishedOfferAndPlaceOrder() throws Exception {
        // Mamy w bazie przygotowaną i opublikowaną ofertę dla klienta
        OfferId offerId = new OfferId("OFF-555");
        Offer offer = new Offer(offerId, new salon.sales.application.domain.model.customer.CustomerId("CUST-1"),
                new SpecificationId("SPEC-1"), Money.of(200000, "PLN"));
        offer.publishOffer();
        offerRepository.save(offer);

        // Klient klika „Akceptuj" na stronie
        mockMvc.perform(post("/api/sales/offers/OFF-555/accept")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Stan oferty w bazie musi zmienić się na ACCEPTED
        Offer savedOffer = offerRepository.findById(offerId).orElseThrow();
        assertThat(savedOffer.state()).isEqualTo(OfferState.ACCEPTED);

        // W bazie musiało powstać nowe zamówienie!
        Order newlyCreatedOrder = orderRepository.findByOfferId(offerId).orElseThrow();
        assertThat(newlyCreatedOrder.state()).isEqualTo(OrderState.DRAFT_CREATED);

        // Domena wysłała zdarzenie złożenia zamówienia do RabbitMQ
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("order.placed"), any(Object.class)
        );
    }

    // ===================================================================================
    // UC-CRM-03: Anulowanie zamówienia (Klient się wycofuje)
    // ===================================================================================
    @Test
    void uc03_shouldCancelOrderAndReleaseInventory() throws Exception {
        // W bazie istnieje zamówienie
        OrderId orderId = new OrderId("ORD-777");
        Order order = new Order(orderId, new OfferId("OFF-777"), Money.of(100000, "PLN"));
        orderRepository.save(order);

        String payload = "{ \"reason\": \"Loss of financial capacity\" }";

        // Sprzedawca klika „Anuluj" w systemie CRM
        mockMvc.perform(post("/api/sales/orders/ORD-777/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // Agregat w bazie ma status CANCELLED
        Order cancelledOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(cancelledOrder.state()).isEqualTo(OrderState.CANCELLED);

        // Powiadomiliśmy systemy zewnętrzne o anulowaniu wraz z odpowiednim powodem
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("order.cancelled"), any(Object.class)
        );
    }

    // ===================================================================================
    // UC-CRM-04: Powiadomienie o gotowości do wydania (Zdarzenie zewnętrzne -> CRM)
    // ===================================================================================
    @Test
    void uc04_shouldMarkOrderAsReadyWhenVehicleArrivesFromLogistics() throws Exception {
        // Mamy aktywne zamówienie w produkcji
        OrderId orderId = new OrderId("ORD-888");
        Order order = new Order(orderId, new OfferId("OFF-888"), Money.of(250000, "PLN"));
        order.activate(); // Stan: IN_PROGRESS
        orderRepository.save(order);

        // Z zewnątrz systemu (Magazyn) przychodzi zdarzenie, że fizyczny samochód trafił na plac
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

        // Stan zamówienia w agregacie zmienił się na READY_FOR_HANDOVER
        Order readyOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(readyOrder.state()).isEqualTo(OrderState.READY_FOR_HANDOVER);

        // Moduł sprzedaży z kolei publikuje własne zdarzenie
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("order.ready_for_handover"), any(Object.class)
        );
    }

    // ===================================================================================
    // UC-SPR-08: Fizyczne wydanie pojazdu i zamknięcie księgowości
    // ===================================================================================
    @Test
    void uc05_shouldHandoverVehicleAndNotifyBilling() throws Exception {
        // Samochód czeka na placu, gotowy do wydania
        OrderId orderId = new OrderId("ORD-999");
        Order order = new Order(orderId, new OfferId("OFF-999"), Money.of(300000, "PLN"));
        order.activate();
        order.markAsReadyForHandover(); // Stan: READY_FOR_HANDOVER
        orderRepository.save(order);

        // Magazyn przyjmuje komendę fizycznego wydania
        stubFor(WireMock.post(urlEqualTo("/api/inventory/releases"))
                .willReturn(aResponse().withStatus(200)));

        // Sprzedawca przekazuje kluczyki i klika „Wydano" w systemie
        mockMvc.perform(post("/api/sales/orders/ORD-999/handover")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Stan zamówienia staje się COMPLETED
        Order completedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(completedOrder.state()).isEqualTo(OrderState.COMPLETED);

        // Zdarzenie dla modułów Rozliczeń / Posprzedażowych
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("vehicle.handed_over"), any(Object.class)
        );
    }
}
