package integration.driving_adapetrs.rest_adapter.billing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import salon.billing.application.service.PaymentAppService;
import salon.sales.domain.exceptions.OfferExpiredException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentRestApiAdapter.class)
class PaymentRestAdapterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentAppService paymentAppService;

    // 1. HAPPY PATH: Zaksięgowanie poprawnej wpłaty
    @Test
    void shouldReturn200OkWhenPaymentIsSuccessfullyRegistered() throws Exception {
        String validJson = """
            {
                "orderId": "ORD-100",
                "amount": 5000.00,
                "currency": "PLN"
            }
            """;

        mockMvc.perform(post("/api/billing/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());

        verify(paymentAppService, times(1)).registerPayment(any());
    }

    // 2. WALIDACJA WEJŚCIA: Kwota ujemna (niedopuszczalne!)
    @Test
    void shouldReturn400BadRequestWhenPaymentAmountIsNegative() throws Exception {
        String invalidJson = """
            {
                "orderId": "ORD-100",
                "amount": -50.00,
                "currency": "PLN"
            }
            """;

        mockMvc.perform(post("/api/billing/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").value("Kwota musi być większa od zera"));

        verify(paymentAppService, never()).registerPayment(any());
    }

    // 3. BŁĄD BIZNESOWY: Wpłata do nieistniejącego zamówienia
    @Test
    void shouldReturn404NotFoundWhenOrderDoesNotExist() throws Exception {
        String validJson = "{ \"orderId\": \"ORD-X\", \"amount\": 1000.00, \"currency\": \"PLN\" }";

        doThrow(new OfferExpiredException("Nie znaleziono zamówienia ORD-X"))
                .when(paymentAppService).registerPayment(any());

        mockMvc.perform(post("/api/billing/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isNotFound()) // Kod 404
                .andExpect(jsonPath("$.error").value("Nie znaleziono zamówienia ORD-X"));
    }
}