package salon.billing.application.command;

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
