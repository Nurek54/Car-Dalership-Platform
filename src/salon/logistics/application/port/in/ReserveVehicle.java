package salon.logistics.application.port.in;

public interface ReserveVehicle {

    void reserveVehicleForOrder(String orderId);

    void orderVehicleFromFactory(String orderId);
}
