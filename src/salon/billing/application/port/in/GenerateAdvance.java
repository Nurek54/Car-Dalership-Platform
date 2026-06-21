package salon.billing.application.port.in;

import salon.billing.application.command.GenerateAdvanceCommand;

/**
 * INBOUND PORT (Fig. 48 — GenerateAdvance) — UC-FIR-01: Sending the deposit request.
 *
 * Triggered by the VehicleIsNotOnStock event (no vehicle in the yard). Prepares the transfer data,
 * notifies the customer and emits AdvancePaymentRequested.
 */
public interface GenerateAdvance {

    /** @return identifier of the generated document (the deposit request). */
    String generateAdvance(GenerateAdvanceCommand command);
}
