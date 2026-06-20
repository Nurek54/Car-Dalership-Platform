package salon.logistics.application.port.in;

/**
 * Port wejściowy UC-INW-06: zdjęcie pojazdu ze stanu magazynowego —
 * węzeł "ReleaseVehicle" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 *
 * Wyzwalany komendą ReleaseVehicle z modułu Sprzedaży/CRM (UC-CRM-05). Kończy się
 * emisją VehicleInventoryReleased lub VehicleInventoryReleasedError (A1: zły status).
 */
public interface ReleaseVehicle {

    void releaseVehicle(String orderId);
}
