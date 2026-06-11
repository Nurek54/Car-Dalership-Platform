package salon.sales.infrastructure.messaging;

import salon.billing.domain.event.AdvancePaymentRequestedEvent;
import salon.billing.domain.event.InvoiceCreatedEvent;
import salon.billing.domain.event.PaymentRegisteredEvent;
import salon.sales.application.port.in.ActivateOrderOnDepositUseCase;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Kontekstu Fakturowania i Rozliczeń
 * w Kontekście Sprzedaży (komunikacja wg kanwy: AdvancePaymentRequested, InvoiceCreated,
 * PaymentRegistered płyną do Sprzedaży i CRM).
 *
 * PaymentRegistered (pierwsza zaksięgowana wpłata/zadatek) aktywuje zamówienie
 * (UC-CRM-03 cz.2, Rys. 19/20 PDF). Pozostałe zdarzenia służą informowaniu Handlowca
 * o postępie rozliczeń.
 */
public class BillingEventSubscriberAdapter {

    private final ActivateOrderOnDepositUseCase activateOrder;

    public BillingEventSubscriberAdapter(ActivateOrderOnDepositUseCase activateOrder) {
        if (activateOrder == null) {
            throw new IllegalArgumentException("activateOrder must not be null.");
        }
        this.activateOrder = activateOrder;
    }

    /** Zaksięgowano wpłatę klienta — aktywacja zamówienia (idempotentna po stronie usługi). */
    public void handlePaymentRegistered(PaymentRegisteredEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator zamówienia (orderId) jest wymagany");
        }
        activateOrder.activateOnDeposit(event.orderId());
    }

    /** Rozliczenia poprosiły klienta o zadatek — informacja dla Handlowca (bez zmiany stanu). */
    public void handleAdvancePaymentRequested(AdvancePaymentRequestedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        System.out.println("[BillingEventSubscriberAdapter] Klient zamówienia " + event.orderId()
                + " został poproszony o wpłatę zadatku.");
    }

    /** Wystawiono fakturę końcową — informacja dla Handlowca (bez zmiany stanu). */
    public void handleInvoiceCreated(InvoiceCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        System.out.println("[BillingEventSubscriberAdapter] Wystawiono fakturę dla zamówienia "
                + event.orderId() + ".");
    }
}
