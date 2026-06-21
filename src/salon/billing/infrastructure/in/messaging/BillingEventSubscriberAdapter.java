package salon.billing.infrastructure.in.messaging;

import salon.billing.application.command.GenerateAdvanceCommand;
import salon.billing.application.command.GenerateInvoiceCommand;
import salon.billing.application.port.in.GenerateAdvance;
import salon.billing.application.port.in.GenerateInvoice;

/**
 * INBOUND ADAPTER (Fig. 48 — EventListener) — subscriber of the Inventory and Logistics events,
 * that trigger document issuance:
 *  - VehicleIsNotOnStock     -> GenerateAdvance  (UC-FIR-01: deposit request),
 *  - VehicleReservedFromStock -> GenerateInvoice (UC-FIR-02: final invoice).
 *
 * ACL: we represent external events as local records and translate them into port commands.
 * authorizedIssuer (e.g. the accountant's e-mail) injected from configuration as the document issuer.
 */
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

    /** UC-FIR-01: no vehicle in the yard -> deposit request. */
    public void handleVehicleIsNotOnStock(VehicleIsNotOnStock event) {
        requireOrderId(event == null ? null : event.orderId());
        this.generateAdvance.generateAdvance(
                new GenerateAdvanceCommand(event.orderId(), this.authorizedIssuer));
    }

    /** UC-FIR-02: vehicle reserved from the yard -> final invoice. */
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

    /** Local (ACL) representations of events from the Inventory and Logistics Context. */
    public record VehicleIsNotOnStock(String orderId) {
    }

    public record VehicleReservedFromStock(String orderId, String vin) {
    }
}
