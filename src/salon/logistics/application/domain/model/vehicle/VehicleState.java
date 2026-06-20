package salon.logistics.application.domain.model.vehicle;

/**
 * Stan cyklu życia egzemplarza pojazdu w Inwentarzu (model strukturalny, Rysunek 38).
 *
 * ON_STOCK ("Wolny") – dostępny na placu, bez rezerwacji;
 * IN_PRODUCTION ("W produkcji") – zlecony w fabryce, jeszcze nie na placu (UC-INW-02);
 * RESERVED ("Zarezerwowany") – twarda blokada na VIN dla konkretnego zamówienia;
 * READY_FOR_HANDOVER ("Gotowy do wydania") – saldo rozliczone, oczekuje na odbiór (UC-INW-05);
 * HANDED_OVER ("Wydany") – wyksięgowany z aktywnego stanu magazynowego (UC-INW-06).
 *
 * READY_FOR_HANDOVER wynika wprost z przypadków użycia UC-INW-05/06 (PDF); diagram klas
 * przedstawia stany w wersji uproszczonej.
 */
public enum VehicleState {
    ON_STOCK,
    IN_PRODUCTION,
    RESERVED,
    READY_FOR_HANDOVER,
    HANDED_OVER
}
