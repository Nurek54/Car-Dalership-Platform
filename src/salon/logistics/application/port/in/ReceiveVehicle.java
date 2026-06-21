package salon.logistics.application.port.in;

/**
 * INBOUND PORT (Figure 37) – "ReceiveVehicle".
 *
 * UC-INW-03: receiving a physical vehicle into the yard (VIN scan on unloading from the transporter)
 * and matching it with the pending order.
 */
public interface ReceiveVehicle {

    void receiveVehicle(String vin);
}
