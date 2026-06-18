package salon.sales.application.port.in;

import salon.common.model.OrderId;

/**
 * Port wejściowy dla UC-CRM-05 (rejestracja fizycznego wydania pojazdu) —
 * węzeł "ReleaseVehicle" w docs/Architecture/SalesArchitecture.md (PDF rozdz. 3.3.3).
 *
 * Zamyka transakcję (zamówienie -> "Zrealizowane"), wysyła komendę ReleaseVehicle
 * do Kontekstu Inwentarza i zleca Rozliczeniom domknięcie salda.
 */
public interface ReleaseVehicle {

    void confirmHandover(OrderId orderId);
}
