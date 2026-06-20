package salon.billing.application.port.in;

import salon.billing.application.command.ProcessPaymentCommand;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * PORT WEJSCIOWY (Rys. 48 — ProcessPayment) — UC-FIR-03: Procesowanie platnosci.
 *
 * Glowne wejscie salda zamowienia: inicjalizacja salda (zdarzenie z Kontekstu Sprzedazy),
 * ksiegowanie przelewu z wyciagu (adapter REST ksiegowego) oraz cykliczne przypomnienia o platnosci.
 */
public interface ProcessPayment {

    /** Inicjalizacja salda dla nowego zamowienia (wartosc kontraktu). */
    void initializeSettlement(OrderId orderId, Money totalAmount);

    /** UC-FIR-03: zaksiegowanie sparowanego przelewu i przeliczenie salda. */
    void processPayment(ProcessPaymentCommand command);

    /** Zadanie cykliczne: przypomnienia o niezaplaconych saldach. */
    void sendPaymentReminders();
}
