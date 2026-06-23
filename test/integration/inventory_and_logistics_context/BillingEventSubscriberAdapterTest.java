package integration.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.logistics.application.port.in.PrepareForHandover;
import salon.logistics.application.port.in.ReleaseVehicle;
import salon.logistics.application.port.in.ReserveVehicle;
import salon.logistics.infrastructure.in.messaging.BillingEventSubscriberAdapter;

import static org.mockito.Mockito.verify;

/** UC-INW-02/04/05: Adapter nasłuchujący zdarzeń Fakturowania — produkcja, zwolnienie, gotowość. */
@SpringBootTest(classes = BillingEventSubscriberAdapter.class)
class BillingEventSubscriberAdapterTest {

    @Autowired private BillingEventSubscriberAdapter adapter;
    @MockBean private ReserveVehicle reserveVehicle;
    @MockBean private PrepareForHandover prepareForHandover;
    @MockBean private ReleaseVehicle releaseVehicle;

    @Test
    void shouldOrderFromFactoryOnAdvancePaymentRegistered() { // UC-INW-02
        adapter.handleAdvancePaymentRegistered(
                new BillingEventSubscriberAdapter.AdvancePaymentRegistered("ORD-1"));

        verify(reserveVehicle).orderVehicleFromFactory("ORD-1");
    }

    @Test
    void shouldPrepareForHandoverOnSettlementCompleted() { // UC-INW-05
        adapter.handleSettlementCompleted(
                new BillingEventSubscriberAdapter.SettlementCompleted("ORD-2"));

        verify(prepareForHandover).prepareVehicleForHandover("ORD-2");
    }

    @Test
    void shouldReleaseReservationOnPaymentDeadlineExpired() { // UC-INW-04
        adapter.handlePaymentDeadlineExpired(
                new BillingEventSubscriberAdapter.PaymentDeadlineExpired("ORD-3"));

        verify(releaseVehicle).releaseReservation("ORD-3");
    }
}
