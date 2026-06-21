package salon.logistics.application.port.in;

/**
 * INBOUND PORT (Figure 37) – "ReleaseVehicle".
 *
 * Releasing the vehicle: removal from stock after physical handover (UC-INW-06) and automatic
 * removal of the lock after the payment deadline is exceeded (UC-INW-04). Both operations "free up"
 * the VIN — respectively to the "Handed over" state or back to the yard as "Free".
 */
public interface ReleaseVehicle {

    /** UC-INW-06: removing the vehicle from the active stock after handover. */
    void releaseVehicle(String orderId);

    /** UC-INW-04: automatic release of the reservation after the payment deadline is exceeded. */
    void releaseReservation(String orderId);
}
