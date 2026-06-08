package salon.financing.application.port.out;

import salon.financing.domain.model.insurance.InsurancePolicy;
import salon.financing.domain.model.insurance.PolicyId;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository {
    void save(InsurancePolicy policy);
    Optional<InsurancePolicy> findById(PolicyId id);
    List<InsurancePolicy> findAll();
}
