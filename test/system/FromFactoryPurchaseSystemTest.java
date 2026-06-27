package system;

import org.junit.jupiter.api.Test;
import salon.billing.application.domain.event.AdvancePaymentRegisteredEvent;
import salon.billing.application.domain.event.AdvancePaymentRequestedEvent;
import salon.logistics.application.domain.event.FactoryOrderPlacedEvent;
import salon.logistics.application.domain.event.VehicleDeliveredToStockEvent;
import salon.logistics.application.domain.event.VehicleIsNotOnStockEvent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test systemowy: ścieżka zakupu pojazdu NIEDOSTĘPNEGO na placu — prośba o zadatek, zlecenie
 * produkcji w fabryce i przyjęcie auta na plac. Spina Sprzedaż, Logistykę i Fakturowanie.
 */
class FromFactoryPurchaseSystemTest {

    @Test
    void shouldOrderVehicleFromFactoryWhenNotOnStock() {
        SalonSystem salon = new SalonSystem();

        // Klient w CRM, brak pojazdu o specyfikacji SPEC-2 na placu
        salon.registerCustomer("CUST-2", "Anna Nowak", "9876543210");

        // Sprzedaż: oferta i akceptacja (przelew)
        String offerId = salon.createOffer("CUST-2", "SPEC-2", 200000);
        String orderId = salon.acceptOfferBankTransfer(offerId);

        // Fakturowanie: otwarcie należności
        salon.initBillingSettlement(orderId, 200000);

        // Logistyka: brak auta na placu -> emisja VehicleNotOnStock
        salon.linkOrderToSpecification(orderId, "SPEC-2");
        salon.reserveOnBankTransfer(orderId);
        assertThat(salon.events.has(VehicleIsNotOnStockEvent.class)).isTrue();

        // Fakturowanie: prośba o zadatek (brak pojazdu na placu)
        salon.billingAdvanceOnNotOnStock(orderId);
        assertThat(salon.events.has(AdvancePaymentRequestedEvent.class)).isTrue();
        assertThat(salon.documentsFor(orderId)).isGreaterThanOrEqualTo(1);

        // Klient wpłaca zadatek (10% = 20 000) -> zaksięgowanie zadatku
        salon.payOrder(orderId, 20000);
        assertThat(salon.settlementStatus(orderId)).isEqualTo("PARTIAL_PAYMENT");
        assertThat(salon.events.has(AdvancePaymentRegisteredEvent.class)).isTrue();

        // Logistyka: zaksięgowany zadatek -> zlecenie produkcji w fabryce
        salon.factoryOrderOnAdvanceRegistered(orderId);
        assertThat(salon.events.has(FactoryOrderPlacedEvent.class)).isTrue();
        assertThat(salon.vehicleStateByOrder(orderId)).isEqualTo("IN_PRODUCTION");

        // Logistyka: auto zjeżdża z lawety na plac (VIN nadany przez fabrykę)
        salon.receiveVehicleAtYard("VIN-FAC-" + orderId);
        assertThat(salon.vehicleStateByOrder(orderId)).isEqualTo("RESERVED");
        assertThat(salon.events.has(VehicleDeliveredToStockEvent.class)).isTrue();
    }
}
