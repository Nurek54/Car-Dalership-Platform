package salon.financing.application.port.out;

import salon.financing.application.domain.model.financing.ApplicationId;
import salon.financing.application.domain.model.financing.FinancingApplication;

import java.util.List;
import java.util.Optional;

public interface FinancingApplicationDatabaseRepository {
    void save(FinancingApplication application);
    Optional<FinancingApplication> findById(ApplicationId id);
    List<FinancingApplication> findAll();
}
