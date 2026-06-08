package salon.financing.application.port.out;

import salon.financing.domain.model.financing.ApplicationId;
import salon.financing.domain.model.financing.FinancingApplication;

import java.util.List;
import java.util.Optional;

public interface FinancingRepository {
    void save(FinancingApplication application);
    Optional<FinancingApplication> findById(ApplicationId id);
    List<FinancingApplication> findAll();
}
