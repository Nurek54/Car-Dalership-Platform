package salon.sales.application.port.out;

/**
 * Port wyjściowy (driven) do systemów realizacji (Inwentarz/fabryka):
 * aktywowane zamówienie uruchamia realizację pojazdu (zlecenie produkcyjne).
 */
public interface ManufacturingIntegrationPort {

    void startVehicleRealization(String orderId);
}
