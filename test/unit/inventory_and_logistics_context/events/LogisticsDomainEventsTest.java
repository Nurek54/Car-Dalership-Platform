package unit.inventory_and_logistics_context.events;

import org.junit.jupiter.api.Test;
import salon.logistics.application.domain.event.FactoryOrderFailedEvent;
import salon.logistics.application.domain.event.VehicleDeliveredToStockEvent;
import salon.logistics.application.domain.event.VehicleInventoryReleasedEvent;
import salon.logistics.application.domain.event.VehicleReadyForHandoverEvent;
import salon.logistics.application.domain.event.VehicleReservationCancelledEvent;
import salon.logistics.application.domain.event.FactoryOrderPlacedEvent;
import salon.logistics.application.domain.event.VehicleInventoryReleasedErrorEvent;
import salon.logistics.application.domain.event.VehicleIsNotOnStockEvent;
import salon.logistics.application.domain.event.VehicleReservedFromStockEvent;

import static org.assertj.core.api.Assertions.*;

/** Zdarzenia dziedzinowe kontekstu Inwentarza i Logistyki (payload + automatyczne metadane). */
class LogisticsDomainEventsTest {

    @Test
    void factoryOrderPlacedShouldCarryOrderAndVin() {
        // Wygodny konstruktor uzupełnia eventId i znacznik czasu
        FactoryOrderPlacedEvent event = new FactoryOrderPlacedEvent("ORD-1", "VIN-1");

        assertThat(event.orderId()).isEqualTo("ORD-1");
        assertThat(event.vin()).isEqualTo("VIN-1");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredOn()).isNotNull();
    }

    @Test
    void reservedFromStockShouldCarryOrderAndVin() {
        VehicleReservedFromStockEvent event = new VehicleReservedFromStockEvent("ORD-2", "VIN-2");

        assertThat(event.orderId()).isEqualTo("ORD-2");
        assertThat(event.vin()).isEqualTo("VIN-2");
        assertThat(event.eventId()).isNotNull();
    }

    @Test
    void notOnStockShouldCarryOnlyOrder() {
        // Zdarzenie braku auta na placu niesie wyłącznie identyfikator zamówienia
        VehicleIsNotOnStockEvent event = new VehicleIsNotOnStockEvent("ORD-3");

        assertThat(event.orderId()).isEqualTo("ORD-3");
        assertThat(event.occurredOn()).isNotNull();
    }

    @Test
    void failureEventsShouldCarryReason() {
        // Zdarzenia błędów niosą powód dla wsparcia/kompensacji
        FactoryOrderFailedEvent factoryFailed = new FactoryOrderFailedEvent("ORD-4", "Brak połączenia");
        VehicleInventoryReleasedErrorEvent releaseError =
                new VehicleInventoryReleasedErrorEvent("ORD-5", "Niewłaściwy status");

        assertThat(factoryFailed.reason()).isEqualTo("Brak połączenia");
        assertThat(factoryFailed.orderId()).isEqualTo("ORD-4");
        assertThat(releaseError.reason()).isEqualTo("Niewłaściwy status");
        assertThat(releaseError.orderId()).isEqualTo("ORD-5");
    }
    @Test
    void deliveredToStockShouldCarryOrderAndVin() {
        // UC-INW-03: kontekst emituje zdarzenie VehicleDeliveredToStock
        VehicleDeliveredToStockEvent event = new VehicleDeliveredToStockEvent("ORD-6", "VIN-6");

        assertThat(event.orderId()).isEqualTo("ORD-6");
        assertThat(event.vin()).isEqualTo("VIN-6");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredOn()).isNotNull();
    }

    @Test
    void reservationCancelledShouldCarryOrderAndVin() {
        // UC-INW-04: kontekst emituje zdarzenie VehicleReservationCancelled
        VehicleReservationCancelledEvent event = new VehicleReservationCancelledEvent("ORD-7", "VIN-7");

        assertThat(event.orderId()).isEqualTo("ORD-7");
        assertThat(event.vin()).isEqualTo("VIN-7");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredOn()).isNotNull();
    }

    @Test
    void readyForHandoverShouldCarryOrderAndVin() {
        // UC-INW-05: kontekst emituje zdarzenie VehicleReadyForHandover
        VehicleReadyForHandoverEvent event = new VehicleReadyForHandoverEvent("ORD-8", "VIN-8");

        assertThat(event.orderId()).isEqualTo("ORD-8");
        assertThat(event.vin()).isEqualTo("VIN-8");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredOn()).isNotNull();
    }

    @Test
    void inventoryReleasedShouldCarryOrderAndVin() {
        // UC-INW-06: kontekst emituje zdarzenie VehicleInventoryReleased
        VehicleInventoryReleasedEvent event = new VehicleInventoryReleasedEvent("ORD-9", "VIN-9");

        assertThat(event.orderId()).isEqualTo("ORD-9");
        assertThat(event.vin()).isEqualTo("VIN-9");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredOn()).isNotNull();
    }
}
