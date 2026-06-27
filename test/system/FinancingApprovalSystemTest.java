package system;

import org.junit.jupiter.api.Test;
import salon.financing.application.domain.event.FinancingApprovedEvent;
import salon.logistics.application.domain.event.VehicleReservedFromStockEvent;
import salon.sales.application.domain.event.FinancingRequestedEvent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test systemowy: ścieżka zakupu z FINANSOWANIEM. Spina Sprzedaż, Finansowanie i Logistykę.
 * Kontekst Finansowania dociąga dane nabywcy i cenę oferty z REALNEJ fasady Sprzedaży (ACL).
 */
class FinancingApprovalSystemTest {

    @Test
    void shouldApproveFinancingAndReserveVehicle() {
        SalonSystem salon = new SalonSystem();

        // Klient w CRM, wolny pojazd na placu o specyfikacji SPEC-3
        salon.registerCustomer("CUST-3", "Piotr Zięba", "5555555555");
        salon.seedStockVehicle("VIN-3", "SPEC-3");

        // Sprzedaż: oferta i akceptacja z wyborem finansowania
        String offerId = salon.createOffer("CUST-3", "SPEC-3", 150000);
        String orderId = salon.acceptOfferFinancing(offerId);
        assertThat(salon.events.has(FinancingRequestedEvent.class)).isTrue();

        // Finansowanie: złożenie wniosku (dane nabywcy i cena pobrane ze Sprzedaży przez ACL)
        salon.requestFinancing(orderId, "CUST-3");
        assertThat(salon.financingState(orderId)).isEqualTo("PENDING");

        // Finansowanie: bank zatwierdza wniosek
        salon.bankDecision(orderId, true);
        assertThat(salon.financingState(orderId)).isEqualTo("APPROVED");
        assertThat(salon.events.has(FinancingApprovedEvent.class)).isTrue();

        // Logistyka: zatwierdzone finansowanie -> rezerwacja pojazdu z placu
        salon.linkOrderToSpecification(orderId, "SPEC-3");
        salon.reserveOnFinancingApproved(orderId);
        assertThat(salon.vehicleStateByOrder(orderId)).isEqualTo("RESERVED");
        assertThat(salon.events.has(VehicleReservedFromStockEvent.class)).isTrue();
    }
}
