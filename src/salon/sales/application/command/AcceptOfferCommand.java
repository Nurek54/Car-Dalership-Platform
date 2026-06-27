package salon.sales.application.command;

import salon.sales.application.domain.model.order.PaymentMethod;

public record AcceptOfferCommand(String offerId, PaymentMethod paymentMethod) {

    public AcceptOfferCommand {
        if (offerId == null || offerId.isBlank()) {
            throw new IllegalArgumentException("offerId must not be blank.");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("paymentMethod must not be null.");
        }
    }
}
