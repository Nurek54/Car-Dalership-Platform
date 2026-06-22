package salon.logistics.application.port.in;

public interface ReleaseVehicle {

    void releaseVehicle(String orderId);

    void releaseReservation(String orderId);
}
