package salon.logistics.application.port.out;

import salon.logistics.application.domain.exception.FactoryOrderFailedException;

import java.util.List;

public interface ImporterACL {

    String placeFactoryOrder(String orderId, List<String> optionCodes);
}
