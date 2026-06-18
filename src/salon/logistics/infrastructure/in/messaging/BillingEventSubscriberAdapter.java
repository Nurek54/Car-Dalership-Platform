package salon.logistics.infrastructure.in.messaging;

import salon.billing.application.domain.event.AdvancePaymentRegisteredEvent;
import salon.billing.application.domain.event.PaymentDeadlineExpiredEvent;
import salon.billing.application.domain.event.SettlementCompletedEvent;
import salon.logistics.application.port.in.OrderFactoryVehicleUseCase;
import salon.logistics.application.port.in.PrepareForHandover;
import salon.logistics.application.port.in.ReleaseReservationUseCase;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Kontekstu Fakturowania i Rozliczeń
 * w Kontekście Inwentarza i Logistyki (komunikacja wg kanwy: AdvancePaymentRegistered,
 * SettlementCompleted, PaymentDeadlineExpired).
 *
 * Kontekst nie posiada własnego mechanizmu odliczania czasu (timerów) — całkowicie
 * polega na zewnętrznym sygnale PaymentDeadlineExpired z modułu Fakturowania.
 */
public class BillingEventSubscriberAdapter {

    private final OrderFactoryVehicleUseCase orderFactoryVehicle;
    private final ReleaseReservationUseCase releaseReservation;
    private final PrepareForHandover prepareForHandover;

    public BillingEventSubscriberAdapter(OrderFactoryVehicleUseCase orderFactoryVehicle,
                                         ReleaseReservationUseCase releaseReservation,
                                         PrepareForHandover prepareForHandover) {
        if (orderFactoryVehicle == null) {
            throw new IllegalArgumentException("orderFactoryVehicle must not be null.");
        }
        if (releaseReservation == null) {
            throw new IllegalArgumentException("releaseReservation must not be null.");
        }
        if (prepareForHandover == null) {
            throw new IllegalArgumentException("prepareForHandover must not be null.");
        }
        this.orderFactoryVehicle = orderFactoryVehicle;
        this.releaseReservation = releaseReservation;
        this.prepareForHandover = prepareForHandover;
    }

    /** UC-INW-02: opłacony zadatek -> zlecenie produkcji pojazdu w fabryce. */
    public void handleAdvancePaymentRegistered(AdvancePaymentRegisteredEvent event) {
        requireOrderId(event == null ? null : event.orderId());
        this.orderFactoryVehicle.orderVehicleFromFactory(event.orderId());
    }

    /** UC-INW-04: przekroczony termin płatności -> zwolnienie blokady pojazdu. */
    public void handlePaymentDeadlineExpired(PaymentDeadlineExpiredEvent event) {
        requireOrderId(event == null ? null : event.orderId());
        this.releaseReservation.releaseReservationForOrder(event.orderId());
    }

    /** UC-INW-05: saldo rozliczone -> pojazd "Gotowy do wydania". */
    public void handleSettlementCompleted(SettlementCompletedEvent event) {
        requireOrderId(event == null ? null : event.orderId());
        this.prepareForHandover.prepareVehicleForHandover(event.orderId());
    }

    private void requireOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Identyfikator zamówienia (orderId) jest wymagany");
        }
    }
}
