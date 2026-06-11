package salon.sales.application.port.in;

import salon.shared.model.OrderId;

/**
 * Port wejściowy dla UC-CRM-05 (rejestracja fizycznego wydania pojazdu) —
 * węzeł "ReleaseVehicleUseCase" w docs/Architecture/SalesArchitecture.md (PDF rozdz. 3.3.3).
 *
 * Zamyka transakcję (zamówienie -> "Zrealizowane"), wysyła komendę ReleaseVehicle
 * do Kontekstu Inwentarza i zleca Rozliczeniom domknięcie salda.
 */
public interface ReleaseVehicleUseCase {

    void confirmHandover(OrderId orderId);
}
