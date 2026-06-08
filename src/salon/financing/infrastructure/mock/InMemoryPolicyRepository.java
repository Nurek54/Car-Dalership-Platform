package salon.financing.infrastructure.mock;

import salon.financing.application.port.out.PolicyRepository;
import salon.financing.domain.model.insurance.InsurancePolicy;
import salon.financing.domain.model.insurance.PolicyId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryPolicyRepository implements PolicyRepository {

    private final Map<PolicyId, InsurancePolicy> store = new HashMap<>();

    @Override
    public void save(InsurancePolicy policy) {
        this.store.put(policy.getId(), policy);
    }

    @Override
    public Optional<InsurancePolicy> findById(PolicyId id) {
        return Optional.ofNullable(this.store.get(id));
    }

    @Override
    public List<InsurancePolicy> findAll() {
        return new ArrayList<>(this.store.values());
    }
}
