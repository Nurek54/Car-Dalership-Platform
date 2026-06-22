package salon.sales.application.port.in;

import salon.sales.application.command.AcceptOfferCommand;

/**
 * INBOUND PORT (Figure 22) — "AcceptOffer".
 * UC-CRM-03: accept a published offer and create an order with the declared payment method;
 * returns the new order id.
 */
public interface AcceptOffer {

    String acceptOffer(AcceptOfferCommand command);
}
