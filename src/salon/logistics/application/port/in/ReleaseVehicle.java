package salon.logistics.application.port.in;

/**
 * PORT WEJŚCIOWY (Rysunek 37) – „ReleaseVehicle”.
 *
 * Zwolnienie pojazdu: zdjęcie ze stanu po fizycznym wydaniu (UC-INW-06) oraz automatyczne
 * zdjęcie blokady po przekroczeniu terminu płatności (UC-INW-04). Obie operacje „uwalniają"
 * numer VIN — odpowiednio do stanu „Wydany" lub z powrotem na plac jako „Wolny".
 */
public interface ReleaseVehicle {

    /** UC-INW-06: zdjęcie pojazdu z aktywnego stanu magazynowego po wydaniu. */
    void releaseVehicle(String orderId);

    /** UC-INW-04: automatyczne zwolnienie rezerwacji po przekroczeniu terminu płatności. */
    void releaseReservation(String orderId);
}
