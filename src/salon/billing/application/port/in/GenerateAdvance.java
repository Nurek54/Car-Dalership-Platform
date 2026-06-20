package salon.billing.application.port.in;

import salon.billing.application.command.GenerateAdvanceCommand;

/**
 * PORT WEJSCIOWY (Rys. 48 — GenerateAdvance) — UC-FIR-01: Wyslanie prosby o zadatek.
 *
 * Wyzwalany zdarzeniem VehicleIsNotOnStock (brak pojazdu na placu). Przygotowuje dane do przelewu,
 * powiadamia klienta i emituje AdvancePaymentRequested.
 */
public interface GenerateAdvance {

    /** @return identyfikator wygenerowanego dokumentu (prosby o zadatek). */
    String generateAdvance(GenerateAdvanceCommand command);
}
