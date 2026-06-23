package integration.billing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import salon.billing.application.command.GenerateAdvanceCommand;
import salon.billing.application.command.GenerateInvoiceCommand;
import salon.billing.application.port.in.GenerateAdvance;
import salon.billing.application.port.in.GenerateInvoice;
import salon.billing.infrastructure.in.messaging.BillingEventSubscriberAdapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/** UC-FIR-01/02: Adapter nasłuchujący zdarzeń logistycznych — wyzwala zadatek lub fakturę końcową. */
@SpringBootTest(classes = BillingEventSubscriberAdapterTest.Config.class)
class BillingEventSubscriberAdapterTest {

    @Autowired private BillingEventSubscriberAdapter adapter;
    @MockBean private GenerateAdvance generateAdvance;
    @MockBean private GenerateInvoice generateInvoice;

    @Test
    void shouldGenerateAdvanceOnVehicleIsNotOnStock() { // UC-FIR-01
        // Brak pojazdu na placu -> prośba o zadatek
        adapter.handleVehicleIsNotOnStock(new BillingEventSubscriberAdapter.VehicleIsNotOnStock("ORD-1"));

        verify(generateAdvance).generateAdvance(any(GenerateAdvanceCommand.class));
    }

    @Test
    void shouldGenerateInvoiceOnVehicleReservedFromStock() { // UC-FIR-02
        // Rezerwacja pojazdu z placu -> faktura końcowa
        adapter.handleVehicleReservedFromStock(
                new BillingEventSubscriberAdapter.VehicleReservedFromStock("ORD-2", "VIN-2"));

        verify(generateInvoice).generateInvoice(any(GenerateInvoiceCommand.class));
    }

    @Configuration
    static class Config {
        @Bean
        BillingEventSubscriberAdapter adapter(GenerateAdvance generateAdvance, GenerateInvoice generateInvoice) {
            // Wystawca dokumentów wstrzykiwany jako parametr konfiguracji adaptera
            return new BillingEventSubscriberAdapter(generateAdvance, generateInvoice, "FA-Księgowy");
        }
    }
}
