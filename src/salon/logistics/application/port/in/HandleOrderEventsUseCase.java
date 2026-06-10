package salon.logistics.application.port.in;

import java.util.List;

/**
 * Port wejściowy: reakcje na zdarzenia zamówień ze Sprzedaży (UC-INW-06, 07) —
 * węzeł "HandleOrderEventsUseCase" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 */
public interface HandleOrderEventsUseCase {

    /**
     * UC-INW-07: Fast Track (blokada auta z placu) albo Long Track (slot produkcyjny).
     * Gdy specCodes nie przyszły w zdarzeniu, dociągane są przez SpecificationIntegrationPort.
     */
    void allocateVehicleForOrder(String orderId, List<String> specCodes);

    /** UC-INW-04 (sekwencja): zwolnienie rezerwacji po PaymentDeadlineExpired (idempotentne). */
    void releaseReservationForOrder(String orderId);

    /** UC-INW-06: wydanie pojazdu klientowi (komenda ReleaseVehicle ze Sprzedaży/CRM). */
    void releaseVehicle(String vin);
}
