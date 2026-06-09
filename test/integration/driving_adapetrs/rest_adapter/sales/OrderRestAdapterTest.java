package integration.driving_adapetrs.rest_adapter.sales;

import salon.sales.infrastructure.web.OrderRestApiAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import salon.sales.application.service.OrderAppService;
import salon.sales.domain.exceptions.OfferExpiredException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderRestApiAdapter.class)
class OrderRestAdapterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderAppService orderAppService;

    // 1. HAPPY PATH: Poprawne utworzenie zamówienia
    @Test
    void shouldReturn201CreatedWhenConvertingOfferToOrder() throws Exception {
        String validJson = "{ \"offerId\": \"OFF-123\", \"customerSignature\": \"SIG-999\" }";

        when(orderAppService.createOrderFromOffer(any())).thenReturn("ORD-500");

        mockMvc.perform(post("/api/sales/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/sales/orders/ORD-500"));
    }

    // 2. WALIDACJA WEJŚCIA: Brak wymaganego podpisu klienta
    @Test
    void shouldReturn400BadRequestWhenSignatureIsMissing() throws Exception {
        String invalidJson = "{ \"offerId\": \"OFF-123\" }"; // Brak pola customerSignature

        mockMvc.perform(post("/api/sales/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.customerSignature").exists());

        verify(orderAppService, never()).createOrderFromOffer(any());
    }

    // 3. BŁĄD BIZNESOWY: Próba utworzenia zamówienia z wygasłej oferty
    @Test
    void shouldReturn409ConflictWhenOfferIsExpired() throws Exception {
        String validJson = "{ \"offerId\": \"OFF-999\", \"customerSignature\": \"SIG-111\" }";

        // Symulujemy, że usługa aplikacji rzuca wyjątek dziedzinowy (Business Exception)
        doThrow(new OfferExpiredException("Oferta OFF-999 straciła ważność."))
                .when(orderAppService).createOrderFromOffer(any());

        mockMvc.perform(post("/api/sales/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isConflict()) // Oczekujemy kodu 409 Conflict (lub 422 Unprocessable Entity)
                .andExpect(jsonPath("$.error").value("Oferta OFF-999 straciła ważność."));
    }
}