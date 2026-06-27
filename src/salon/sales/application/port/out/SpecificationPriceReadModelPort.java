package salon.sales.application.port.out;

import salon.common.model.Money;
import salon.common.model.SpecificationId;

import java.util.Optional;

public interface SpecificationPriceReadModelPort {

    Optional<Money> findPrice(SpecificationId specificationId);

    
    void savePrice(SpecificationId specificationId, Money price);
}
