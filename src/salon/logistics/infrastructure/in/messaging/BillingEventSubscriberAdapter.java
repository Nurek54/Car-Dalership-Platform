package salon.logistics.infrastructure.in.messaging;

import salon.logistics.application.port.in.PrepareForHandover;
import salon.logistics.application.port.in.ReleaseVehicle;
import salon.logistics.application.port.in.ReserveVehicle;

public class BillingEventSubscriberAdapter {

    private final ReserveVehicle reserveVehicle;
    private final PrepareForHandover prepareForHandover;
    private final ReleaseVehicle releaseVehicle;

    public BillingEventSubscriberAdapter(ReserveVehicle reserveVehicle,
                                         PrepareForHandover prepareForHandover,
                                         ReleaseVehicle releaseVehicle) {
        if (reserveVehicle == null) {
            throw new IllegalArgumentException("reserveVehicle must not be null.");
        }
        if (prepareForHandover == null) {
            throw new IllegalArgumentException("prepareForHandover must not be null.");
        }
        if (releaseVehicle == null) {
            throw new IllegalArgumentException("releaseVehicle must not be null.");
        }
        this.reserveVehicle = reserveVehicle;
        this.prepareForHandover = prepareForHandover;
        this.releaseVehicle = releaseVehicle;
    }

    public void handleAdvancePaymentRegistered(AdvancePaymentRegistered event) {
        requireOrderId(event == null ? null : event.orderId());
        this.reserveVehicle.orderVehicleFromFactory(event.orderId());
    }

    public void handleSettlementCompleted(SettlementCompleted event) {
        requireOrderId(event == null ? null : event.orderId());
        this.prepareForHandover.prepareVehicleForHandover(event.orderId());
    }

    public void handlePaymentDeadlineExpired(PaymentDeadlineExpired event) {
        requireOrderId(event == null ? null : event.orderId());
        this.releaseVehicle.releaseReservation(event.orderId());
    }

    private static void requireOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
    }

    
    public record AdvancePaymentRegistered(String orderId) {
    }

    public record SettlementCompleted(String orderId) {
    }

    public record PaymentDeadlineExpired(String orderId) {
    }
}
