package salon.sales.application.port.in;

/**
 * Komenda dla UC-CRM-01: Handlowiec inicjuje sesję konfiguratora dla konkretnego klienta.
 */
public record StartConfiguratorSessionCommand(String customerId, String salespersonId) {

    public StartConfiguratorSessionCommand {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank.");
        }
        if (salespersonId == null || salespersonId.isBlank()) {
            throw new IllegalArgumentException("salespersonId must not be blank.");
        }
    }
}
