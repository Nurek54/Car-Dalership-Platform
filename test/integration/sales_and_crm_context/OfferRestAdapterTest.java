package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import salon.sales.infrastructure.in.web.OfferRestApiAdapter;
import salon.sales.application.service.SalesService;
import salon.sales.application.domain.exception.OfferNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(OfferRestApiAdapter.class)
class OfferRestAdapterTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private SalesService salesAppService;

    @Test
    void shouldAcceptOfferAndReturn200Ok() throws Exception {
        // Frontend przysyła żądanie akceptacji oferty przez klienta
        String offerId = "OFF-100";

        // Kontroler przetwarza to żądanie bez błędów i zwraca 200 OK
        mockMvc.perform(post("/api/sales/offers/{id}/accept", offerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404NotFoundWhenOfferIsMissing() throws Exception {
        // Nie ma oferty
        doThrow(new OfferNotFoundException("OFF-999"))
                .when(salesAppService).acceptOfferAndCreateOrder(any());

        // Adapter mapuje ten wyjątek domenowy na kod HTTP 404
        mockMvc.perform(post("/api/sales/offers/OFF-999/accept")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Offer OFF-999 not found in the system"));
    }
}