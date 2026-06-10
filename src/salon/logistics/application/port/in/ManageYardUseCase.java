package salon.logistics.application.port.in;

/**
 * Port wejściowy: zarządzanie placem i PDI (UC-INW-01, 03, 05) —
 * węzeł "ManageYardUseCase" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 */
public interface ManageYardUseCase {

    /** UC-INW-01: przyjęcie fizycznego pojazdu na plac (weryfikacja VIN przez ACL importera). */
    void receiveVehicle(String vin);

    /** UC-INW-03: rejestracja dostawy — auto na placu łączone z oczekującym zamówieniem (lub zostaje wolne). */
    void registerDelivery(String vin);

    /** UC-INW-05: po rozliczeniu salda (SettlementCompleted) — oznaczenie auta jako gotowego do wydania. */
    void markVehicleReadyForHandover(String orderId);
}
