package salon.sales.application.port.out;

/**
 * Port wyjściowy (driven) do obsługi posprzedażowej: wydany pojazd zostaje
 * zarejestrowany w module serwisowym (przeglądy, gwarancja).
 */
public interface AfterSalesIntegrationPort {

    void registerVehicleForAfterSales(String orderId);
}
