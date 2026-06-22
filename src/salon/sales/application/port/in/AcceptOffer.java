package salon.sales.application.port.in;

import salon.sales.application.command.AcceptOfferCommand;

public interface AcceptOffer {

    String acceptOffer(AcceptOfferCommand command);
}
