package salon.logistics.application.port.in;

/**
 * INBOUND PORT (Figure 37) – "ReserveVehicle".
 *
 * Securing a vehicle for the order: reservation from the yard (UC-INW-01), and when the car is not
 * in stock — a production order at the factory (UC-INW-02). Corresponds to the "allocate vehicle
 * or production slot" operation called by the Sales Context.
 */
public interface ReserveVehicle {

    /** UC-INW-01: availability check and hard reservation of a vehicle from the yard. */
    void reserveVehicleForOrder(String orderId);

    /** UC-INW-02: ordering vehicle production at the factory after the deposit is paid. */
    void orderVehicleFromFactory(String orderId);
}
