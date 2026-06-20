package salon.financing.infrastructure.out.mock;

import salon.financing.application.port.out.FinancingApplicationDatabaseRepository;
import salon.financing.application.domain.model.financing.ApplicationId;
import salon.financing.application.domain.model.financing.FinancingApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryFinancingRepository implements FinancingApplicationDatabaseRepository {

    private final Map<ApplicationId, FinancingApplication> store = new HashMap<>();

    @Override
    public void save(FinancingApplication application) {
        this.store.put(application.getId(), application);
    }

    @Override
    public Optional<FinancingApplication> findById(ApplicationId id) {
        return Optional.ofNullable(this.store.get(id));
    }

    @Override
    public List<FinancingApplication> findAll() {
        return new ArrayList<>(this.store.values());
    }
}
