package salon.billing.infrastructure.in.messaging;

import salon.billing.application.command.GenerateAdvanceCommand;
import salon.billing.application.port.in.GenerateAdvance;
import salon.billing.application.command.GenerateInvoiceCommand;
import salon.billing.application.port.in.GenerateInvoice;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter WEJŚCIOWY (driving) sterowany zdarzeniami z Inwentarza — diagramy sekwencji
 * UC-FIR-01 i UC-FIR-02 (lifeline "BillingEventSubscriberAdapter «driving adapter»").
 *
 * VehicleIsNotOnStock      -> generateAdvance (UC-FIR-01, prośba o zadatek)
 * VehicleReservedFromStock -> generateInvoice (UC-FIR-02, faktura końcowa)
 *
 * Zdarzenia niosą tylko orderId/VIN — dane nabywcy DocumentGenerationService dociąga
 * przez SalesIntegration. Idempotencyjność: deduplikacja po eventId.
 */
public class BillingEventSubscriberAdapter {

    private final GenerateAdvance generateAdvance;
    private final GenerateInvoice generateInvoice;
    private final String authorizedIssuer;
    private final Set<UUID> processedEventIds = ConcurrentHashMap.newKeySet();

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

    // UC-FIR-01: brak auta na stocku -> dokument zadatku.
    public void on(VehicleIsNotOnStockEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (isDuplicate(event.eventId())) {
            return;
        }
        this.generateAdvance.generateAdvance(
                new GenerateAdvanceCommand(event.orderId(), this.authorizedIssuer));
    }

    // UC-FIR-02: auto zarezerwowane ze stocku -> faktura końcowa.
    public void on(VehicleReservedFromStockEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (isDuplicate(event.eventId())) {
            return;
        }
        this.generateInvoice.generateInvoice(new GenerateInvoiceCommand(
                event.orderId(),
                "Faktura koncowa - zamowienie " + event.orderId(),
                this.authorizedIssuer));
    }

    private boolean isDuplicate(UUID eventId) {
        boolean firstTime = this.processedEventIds.add(eventId);
        if (!firstTime) {
            System.out.println("[BillingEventSubscriberAdapter] Duplicate event ignored: " + eventId);
        }
        return !firstTime;
    }
}
