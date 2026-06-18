package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import salon.sales.application.service.SalesService;
import salon.sales.application.domain.exception.OrderNotFoundException;
import salon.sales.infrastructure.in.web.OrderRestApiAdapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(OrderRestApiAdapter.class)
class OrderRestAdapterTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private SalesService salesAppService;

    @Test
    void shouldAcceptScheduleHandoverRequestAndReturn200Ok() throws Exception {
        String jsonPayload = "{ \"handoverDate\": \"2026-06-20\" }";

        // Kontroler poprawnie to przetwarza i zwraca 200 OK
        mockMvc.perform(post("/api/sales/orders/ORD-123/schedule-handover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400BadRequestWhenDateIsInvalidOrMissing() throws Exception {
        // JSON bez wymaganej daty
        String invalidJson = "{ \"handoverDate\": \"\" }";

        // Walidacja odrzuca żądanie zanim trafi do domeny
        mockMvc.perform(post("/api/sales/orders/ORD-123/schedule-handover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid input parameters"));
    }

    @Test
    void shouldReturn404NotFoundWhenOrderDoesNotExist() throws Exception {
        // AppService rzuca wyjątek informujący o braku zamówienia
        doThrow(new OrderNotFoundException("Order ORD-999 not found"))
                .when(salesAppService).scheduleHandover(any());

        String jsonPayload = "{ \"handoverDate\": \"2026-06-20\" }";

        mockMvc.perform(post("/api/sales/orders/ORD-999/schedule-handover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order ORD-999 not found"));
    }
}