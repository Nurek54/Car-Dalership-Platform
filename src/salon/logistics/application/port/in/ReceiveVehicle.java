package salon.logistics.application.port.in;

/**
 * Port wejściowy UC-INW-03: przyjęcie pojazdu na stan magazynowy —
 * węzeł "ReceiveVehicle" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 *
 * Wyzwalany przez Pracownika Placu (skan VIN przy zjeździe z lawety). Auto czekające
 * na zamówienie zostaje sparowane (status "Zarezerwowany", VehicleDeliveredToStock);
 * auto "na stock" (A1) pozostaje wolne — bez zdarzenia końcowego.
 */
public interface ReceiveVehicle {

    void receiveVehicle(String vin);
}
