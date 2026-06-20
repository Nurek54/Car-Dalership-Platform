package salon.logistics.application.port.in;

/**
 * PORT WEJŚCIOWY (Rysunek 37) – „ReserveVehicle”.
 *
 * Zapewnienie pojazdu dla zamówienia: rezerwacja z placu (UC-INW-01), a gdy auta nie ma
 * na stanie — zlecenie produkcji w fabryce (UC-INW-02). Odpowiada operacji „allocate vehicle
 * or production slot" wołanej przez Kontekst Sprzedaży.
 */
public interface ReserveVehicle {

    /** UC-INW-01: weryfikacja dostępności i twarda rezerwacja pojazdu z placu. */
    void reserveVehicleForOrder(String orderId);

    /** UC-INW-02: zlecenie produkcji pojazdu w fabryce po opłaceniu zadatku. */
    void orderVehicleFromFactory(String orderId);
}
