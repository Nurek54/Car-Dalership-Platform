package salon.billing.application.port.in;

import salon.billing.application.command.GenerateAdvanceCommand;

// Port wejściowy dla UC-FIR-01: wygenerowanie dokumentu zadatku. Zwraca id dokumentu.
public interface GenerateAdvance {
    String generateAdvance(GenerateAdvanceCommand command);
}
