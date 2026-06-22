package salon.sales.infrastructure.in.web;

import salon.sales.application.command.AcceptOfferCommand;
import salon.sales.application.domain.model.order.PaymentMethod;
import salon.sales.application.port.in.AcceptOffer;

public class OfferRestController {

    private final AcceptOffer acceptOffer;

    public OfferRestController(AcceptOffer acceptOffer) {
        if (acceptOffer == null) {
            throw new IllegalArgumentException("acceptOffer must not be null.");
        }
        this.acceptOffer = acceptOffer;
    }

    public String acceptOffer(String offerId, PaymentMethod paymentMethod) {
        return this.acceptOffer.acceptOffer(new AcceptOfferCommand(offerId, paymentMethod));
    }
}
