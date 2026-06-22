package salon.billing.application.port.in;

import salon.billing.application.command.GenerateAdvanceCommand;

public interface GenerateAdvance {

    String generateAdvance(GenerateAdvanceCommand command);
}
