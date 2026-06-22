package salon.billing.infrastructure.in.messaging;

import salon.billing.application.command.GenerateAdvanceCommand;
import salon.billing.application.command.GenerateInvoiceCommand;
import salon.billing.application.port.in.GenerateAdvance;
import salon.billing.application.port.in.GenerateInvoice;

public class BillingEventSubscriberAdapter {

    private final GenerateAdvance generateAdvance;
    private final GenerateInvoice generateInvoice;
    private final String authorizedIssuer;

    public BillingEventSubscriberAdapter(GenerateAdvance generateAdvance,
                                         GenerateInvoice generateInvoice,
                                         String authorizedIssuer) {
        if (generateAdvance == null) {
            throw new IllegalArgumentException("generateAdvance must not be null.");
        }
        if (generateInvoice == null) {
            throw new IllegalArgumentException("generateInvoice must not be null.");
        }
        if (authorizedIssuer == null || authorizedIssuer.isBlank()) {
            throw new IllegalArgumentException("authorizedIssuer must not be blank.");
        }
        this.generateAdvance = generateAdvance;
        this.generateInvoice = generateInvoice;
        this.authorizedIssuer = authorizedIssuer;
    }

    public void handleVehicleIsNotOnStock(VehicleIsNotOnStock event) {
        requireOrderId(event == null ? null : event.orderId());
        this.generateAdvance.generateAdvance(
                new GenerateAdvanceCommand(event.orderId(), this.authorizedIssuer));
    }

    public void handleVehicleReservedFromStock(VehicleReservedFromStock event) {
        requireOrderId(event == null ? null : event.orderId());
        this.generateInvoice.generateInvoice(new GenerateInvoiceCommand(
                event.orderId(), "Final invoice " + event.orderId(), this.authorizedIssuer));
    }

    private static void requireOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
    }

    public record VehicleIsNotOnStock(String orderId) {
    }

    public record VehicleReservedFromStock(String orderId, String vin) {
    }
}
