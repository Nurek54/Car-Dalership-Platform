package salon.logistics.application.port.in;

/**
 * INBOUND PORT (Figure 37) – "PrepareForHandover".
 *
 * UC-INW-05: after full settlement of the balance (SettlementCompleted) the vehicle's status changes
 * to "Ready for handover".
 */
public interface PrepareForHandover {

    void prepareVehicleForHandover(String orderId);
}
