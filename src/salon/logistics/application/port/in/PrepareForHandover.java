package salon.logistics.application.port.in;

/**
 * PORT WEJŚCIOWY (Rysunek 37) – „PrepareForHandover”.
 *
 * UC-INW-05: po pełnym rozliczeniu salda (SettlementCompleted) zmiana statusu pojazdu
 * na „Gotowy do wydania”.
 */
public interface PrepareForHandover {

    void prepareVehicleForHandover(String orderId);
}
