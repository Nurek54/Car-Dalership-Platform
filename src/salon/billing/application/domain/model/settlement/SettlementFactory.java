package salon.billing.application.domain.model.settlement;

import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * FABRYKA (Rys. 48 — SettlementFactory): powołuje poprawny agregat {@link Settlement}
 * w stanie OPEN, nadając mu nową, globalną tożsamość ({@link SettlementId}). Tworzenie nie jest
 * odpowiedzialnością klienta — fabryka gwarantuje niezmienniki (kompletne, niepuste pola) i
 * nigdy nie zwraca obiektu w niespójnym stanie.
 */
public class SettlementFactory {

    /** Inicjalizacja salda dla zamówienia o znanej wartości kontraktu (UC-CRM-03 -> Rozliczenia). */
    public Settlement createNew(OrderId orderId, Money totalAmount) {
        return new Settlement(SettlementId.generate(), orderId, totalAmount, SettlementStatus.OPEN);
    }
}
