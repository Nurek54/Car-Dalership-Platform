package system;

import org.junit.jupiter.api.Test;
import salon.billing.application.domain.event.InvoiceCreatedEvent;
import salon.billing.application.domain.event.SettlementCompletedEvent;
import salon.logistics.application.domain.event.VehicleInventoryReleasedEvent;
import salon.logistics.application.domain.event.VehicleReadyForHandoverEvent;
import salon.logistics.application.domain.event.VehicleReservedFromStockEvent;
import salon.sales.application.domain.event.OrderPlacedEvent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test systemowy: pełna ścieżka zakupu pojazdu DOSTĘPNEGO NA PLACU, płatność przelewem.
 * Spina kontekst Sprzedaży, Logistyki i Fakturowania end-to-end.
 */
class OnStockPurchaseSystemTest {

    @Test
    void shouldCompletePurchaseOfVehicleFromStock() {
        SalonSystem salon = new SalonSystem();

        // Dane wyjściowe: klient w CRM, wolny pojazd na placu o specyfikacji SPEC-1
        salon.registerCustomer("CUST-1", "Jan Kowalski", "1234563218");
        salon.seedStockVehicle("VIN-1", "SPEC-1");

        // Sprzedaż: oferta proforma i jej akceptacja (przelew) -> powstaje zamówienie
        String offerId = salon.createOffer("CUST-1", "SPEC-1", 120000);
        String orderId = salon.acceptOfferBankTransfer(offerId);
        assertThat(salon.events.has(OrderPlacedEvent.class)).isTrue();

        // Fakturowanie: otwarcie należności dla zamówienia (gotowość do rozliczenia)
        salon.initBillingSettlement(orderId, 120000);

        // Logistyka: powiązanie zamówienia ze specyfikacją i rezerwacja pojazdu z placu
        salon.linkOrderToSpecification(orderId, "SPEC-1");
        salon.reserveOnBankTransfer(orderId);
        assertThat(salon.vehicleStateByOrder(orderId)).isEqualTo("RESERVED");
        assertThat(salon.events.has(VehicleReservedFromStockEvent.class)).isTrue();

        // Fakturowanie: faktura końcowa po rezerwacji pojazdu
        salon.billingInvoiceOnReserved(orderId, "VIN-1");
        assertThat(salon.documentsFor(orderId)).isGreaterThanOrEqualTo(1);
        assertThat(salon.events.has(InvoiceCreatedEvent.class)).isTrue();

        // Fakturowanie: pełna wpłata domyka saldo
        salon.payOrder(orderId, 120000);
        assertThat(salon.settlementStatus(orderId)).isEqualTo("SETTLED");
        assertThat(salon.events.has(SettlementCompletedEvent.class)).isTrue();

        // Logistyka: pełne rozliczenie -> pojazd gotowy do wydania
        salon.prepareForHandoverOnSettlement(orderId);
        assertThat(salon.vehicleStateByOrder(orderId)).isEqualTo("READY_FOR_HANDOVER");
        assertThat(salon.events.has(VehicleReadyForHandoverEvent.class)).isTrue();

        // Sprzedaż: gotowość do wydania -> umówienie i fizyczne wydanie pojazdu
        salon.salesMarkReadyOnVehicleReady(orderId);
        assertThat(salon.salesOrderState(orderId)).isEqualTo("READY_FOR_HANDOVER");
        salon.salesScheduleAndRelease(orderId);
        assertThat(salon.salesOrderState(orderId)).isEqualTo("COMPLETED");

        // Logistyka: zdjęcie pojazdu ze stanu po komendzie wydania ze Sprzedaży
        salon.logisticsReleaseOnSalesCommand(orderId);
        assertThat(salon.vehicleStateByVin("VIN-1")).isEqualTo("HANDED_OVER");
        assertThat(salon.events.has(VehicleInventoryReleasedEvent.class)).isTrue();
    }
}
