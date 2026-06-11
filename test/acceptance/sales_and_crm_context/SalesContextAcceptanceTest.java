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

import salon.sales.domain.model.customer.CustomerId;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.offer.OfferState;
import salon.sales.domain.model.order.Order;
import salon.sales.domain.model.order.OrderState;
import salon.sales.infrastructure.persistence.OfferDatabaseAdapter;
import salon.sales.infrastructure.persistence.OrderDatabaseAdapter;
import salon.sales.domain.event.*;
import salon.shared.model.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8089) // Udajemy resztę firmy (Katalog, Inwentarz, Księgowość) na jednym porcie
class SalesContextAcceptanceTest {

    @Autowired private MockMvc mockMvc; // Zastępuje przeglądarkę / front-end
    @Autowired private ObjectMapper objectMapper; // Do generowania JSON-ów

    @Autowired private OfferDatabaseAdapter offerRepository;
    @Autowired private OrderDatabaseAdapter orderRepository;

    @MockBean private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setupWireMock() {
        // Resetujemy sztuczne serwery przed każdym przypadkiem użycia
        resetAllRequests();
    }

    // ===================================================================================
    // UC-CRM-01: Inicjacja sesji konfiguratora przez Handlowca
    // ===================================================================================
    @Test
    void uc01_shouldInitiateConfiguratorSession() throws Exception {
        // Handlowiec chce otworzyć konfigurator dla konkretnego klienta
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

        // Weryfikujemy, czy system wyemitował poprawne zdarzenie asynchroniczne na kolejkę
        verify(rabbitTemplate).convertAndSend(                    // Domyślnie 1 raz. JAVA na JSON
                eq("sales.events.exchange"),                // Gdzie wysyła
                eq("configurator.session.initiated"),       // Etykieta
                any(ConfiguratorSessionInitiatedEvent.class)      // Ciało wiadomości
        );
    }

    // ===================================================================================
    // UC-CRM-02 & UC-SPR-02: Akceptacja Opublikowanej Oferty i utworzenie Zamówienia
    // ===================================================================================
    @Test
    void uc02_shouldAcceptPublishedOfferAndPlaceOrder() throws Exception {
        // Mamy w bazie danych przygotowaną i opublikowaną ofertę dla klienta
        OfferId offerId = new OfferId("OFF-555");
        Offer offer = new Offer(offerId, new CustomerId("CUST-1"), new SpecificationId("SPEC-1"), Money.of(200000, "PLN"));
        offer.publishOffer();
        offerRepository.save(offer);

        // Inwentarz potwierdzi, że ma wolne miejsce na produkcję (Zewnętrzne API - WireMock)
        stubFor(post(urlEqualTo("/api/inventory/allocations"))
                .willReturn(aResponse().withStatus(201))); // 201 Created

        // Klient klika "Akceptuję" na stronie
        mockMvc.perform(post("/api/sales/offers/OFF-555/accept")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Stan oferty w bazie danych musi się zmienić na ACCEPTED
        Offer savedOffer = offerRepository.findById(offerId).orElseThrow();
        assertThat(savedOffer.getState()).isEqualTo(OfferState.ACCEPTED);

        // Nowe zamówienie musiało powstać w bazie danych!
        Order newlyCreatedOrder = orderRepository.findByOfferId(offerId).orElseThrow();
        assertThat(newlyCreatedOrder.getState()).isEqualTo(OrderState.DRAFT);

        // Domena wysłała Event o złożeniu zamówienia na RabbitMQ
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("order.placed"), any(OrderPlacedEvent.class)
        );
    }

    // ===================================================================================
    // UC-CRM-03: Anulowanie Zamówienia (Klient rezygnuje)
    // ===================================================================================
    @Test
    void uc03_shouldCancelOrderAndReleaseInventory() throws Exception {
        // W bazie jest zamówienie
        OrderId orderId = new OrderId("ORD-777");
        Order order = new Order(orderId, new OfferId("OFF-777"), Money.of(100000, "PLN"));
        orderRepository.save(order);

        String payload = "{ \"reason\": \"Utrata zdolności finansowej\" }";

        // Handlowiec klika "Anuluj" w systemie CRM
        mockMvc.perform(post("/api/sales/orders/ORD-777/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // Agregat w bazie ma status CANCELLED
        Order cancelledOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(cancelledOrder.getState()).isEqualTo(OrderState.CANCELLED);

        // Powiadomiliśmy systemy zewnętrzne o anulowaniu z odpowiednim powodem
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("order.cancelled"), any(OrderCancelledEvent.class)
        );
    }

    // ===================================================================================
    // UC-CRM-04: Powiadomienie o gotowości do odbioru (Zew. Event -> CRM)
    // ===================================================================================
    @Test
    void uc04_shouldMarkOrderAsReadyWhenVehicleArrivesFromLogistics() throws Exception {
        // Mamy aktywne, produkujące się zamówienie
        OrderId orderId = new OrderId("ORD-888");
        Order order = new Order(orderId, new OfferId("OFF-888"), Money.of(250000, "PLN"));
        order.activate(); // Stan: IN_PROGRESS
        orderRepository.save(order);

        // Z zewnątrz systemu (Inwentarza) przychodzi Event, że fizyczne auto zjechało na plac
        String incomingEventJson = """
                {
                    "eventId": "12312312-1231-1231-1231-1231231231231",
                    "vin": "WBA123456789",
                    "orderId": "ORD-888",
                    "occurredOn": "2026-06-11T12:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/sales/webhooks/inventory/vehicle-ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incomingEventJson))
                .andExpect(status().isOk());

        // Stan zamówienia w Agregacie zmienił się na READY_FOR_HANDOVER
        Order readyOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(readyOrder.getState()).isEqualTo(OrderState.READY_FOR_HANDOVER);

        // Moduł sprzedaży wypuszcza z kolei swój Event
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("order.ready_for_handover"), any(OrderReadyForHandoverEvent.class)
        );
    }

    // ===================================================================================
    // UC-SPR-08: Fizyczne wydanie pojazdu i domknięcie księgowości
    // ===================================================================================
    @Test
    void uc05_shouldHandoverVehicleAndNotifyBilling() throws Exception {
        // Auto czeka na placu, umówiono wizytę z klientem
        OrderId orderId = new OrderId("ORD-999");
        Order order = new Order(orderId, new OfferId("OFF-999"), Money.of(300000, "PLN"));
        order.activate();
        order.markAsReadyForHandover(); // Stan: READY_FOR_HANDOVER
        orderRepository.save(order);

        // Serwer księgowości jest gotowy na przyjęcie komendy do wystawienia faktury końcowej
        stubFor(put(urlEqualTo("/api/billing/accounts/ORD-999/close"))
                .willReturn(aResponse().withStatus(200)));

        // Handlowiec wydaje kluczyki i klika w systemie "Wydano"
        mockMvc.perform(post("/api/sales/orders/ORD-999/handover")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Stan zamówienia staje się COMPLETED
        Order completedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(completedOrder.getState()).isEqualTo(OrderState.COMPLETED);

        // Żądanie sieciowe PUT do działu Księgowości
        verify(1, putRequestedFor(urlEqualTo("/api/billing/accounts/ORD-999/close")));

        // Event dla modułu Posprzedażowego
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("vehicle.handed_over"), any(VehicleHandedOverEvent.class)
        );
    }
}