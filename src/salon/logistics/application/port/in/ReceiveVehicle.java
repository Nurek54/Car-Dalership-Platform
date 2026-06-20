package salon.logistics.application.port.in;

/**
 * PORT WEJŚCIOWY (Rysunek 37) – „ReceiveVehicle”.
 *
 * UC-INW-03: przyjęcie fizycznego pojazdu na plac (skan VIN przy zjeździe z lawety)
 * i sparowanie go z oczekującym zamówieniem.
 */
public interface ReceiveVehicle {

    void receiveVehicle(String vin);
}
