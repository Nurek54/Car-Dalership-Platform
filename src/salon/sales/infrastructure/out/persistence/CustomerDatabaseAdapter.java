package salon.sales.infrastructure.out.persistence;

import org.springframework.stereotype.Repository;
import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.port.out.CustomerDatabaseRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OUTBOUND ADAPTER (Figure 22: DBAdapter) — Spring-managed in-memory implementation of the CRM
 * {@link CustomerDatabaseRepository}. Customer master data is not exercised by the persistence
 * slice tests, so a lightweight in-memory store is sufficient for the wired context.
 */
@Repository
public class CustomerDatabaseAdapter implements CustomerDatabaseRepository {

    private final Map<String, Customer> byId = new ConcurrentHashMap<>();

    @Override
    public void save(Customer customer) {
        this.byId.put(customer.getId().value(), customer);
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return Optional.ofNullable(this.byId.get(id.value()));
    }

    @Override
    public boolean existsById(String customerId) {
        return this.byId.containsKey(customerId);
    }
}
