package salon.billing.application.command;

/**
 * Input data model of the GenerateAdvance port (UC-FIR-01).
 * Walidacja syntaktyczna (niebiznesowa) w konstruktorze — zadanie uslugi aplikacji.
 */
public record GenerateAdvanceCommand(String orderId, String authorizedIssuer) {

    public GenerateAdvanceCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (authorizedIssuer == null || authorizedIssuer.isBlank()) {
            throw new IllegalArgumentException("authorizedIssuer must not be blank.");
        }
    }
}
