package integration.driving_adapetrs.rest_adapter.logistics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import salon.logistics.application.InventoryAppService;
import salon.logistics.domain.exceptions.InvalidVehicleStateException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryRestAdapter.class)
class InventoryRestAdapterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryAppService inventoryAppService;

    // 1. HAPPY PATH: Zmiana statusu PDI
    @Test
    void shouldReturn200OkWhenPdiIsApproved() throws Exception {
        String vin = "VIN1234567890ABCDE";

        mockMvc.perform(put("/api/inventory/vehicles/{vin}/pdi-approval", vin))
                .andExpect(status().isOk());

        verify(inventoryAppService, times(1)).approvePdi(vin);
    }

    // 2. WALIDACJA WEJŚCIA: Zły format VIN w URL (np. za krótki)
    @Test
    void shouldReturn400BadRequestWhenVinIsTooShort() throws Exception {
        String invalidVin = "VIN123"; // Mniej niż wymagane 17 znaków

        mockMvc.perform(put("/api/inventory/vehicles/{vin}/pdi-approval", invalidVin))
                .andExpect(status().isBadRequest())
                // Zakładamy, że parametr @PathVariable(pattern) ogranicza długość
                .andExpect(jsonPath("$.error").value("Nieprawidłowy format numeru VIN"));

        verify(inventoryAppService, never()).approvePdi(anyString());
    }

    // 3. BŁĄD BIZNESOWY: Auto już wydane (nie można zrobić PDI)
    @Test
    void shouldReturn422UnprocessableEntityWhenVehicleIsAlreadyHandedOver() throws Exception {
        String vin = "VIN1234567890ABCDE";

        doThrow(new InvalidVehicleStateException("Auto zostało już wydane klientowi, PDI niemożliwe."))
                .when(inventoryAppService).approvePdi(vin);

        mockMvc.perform(put("/api/inventory/vehicles/{vin}/pdi-approval", vin))
                .andExpect(status().isUnprocessableEntity()) // Kod 422 - żądanie zablokowane przez reguły biznesowe
                .andExpect(jsonPath("$.error").value("Auto zostało już wydane klientowi, PDI niemożliwe."));
    }
}