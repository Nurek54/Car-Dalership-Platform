package salon.sales.infrastructure.out.mock;

import org.springframework.stereotype.Component;
import salon.sales.application.port.out.CustomerDatabaseRepository;
import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter wyjściowy (in-memory) portu CustomerDatabaseRepository — do dem i uruchomień bez bazy.
 * Klient nie ma jeszcze adaptera JPA, więc rejestr działa w pamięci procesu.
 */
@Component
public class InMemoryCustomerRepository implements CustomerDatabaseRepository {

    private final Map<CustomerId, Customer> store = new HashMap<>();

    @Override
    public void save(Customer customer) {
        this.store.put(customer.id(), customer);
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return Optional.ofNullable(this.store.get(id));
    }

    @Override
    public boolean existsById(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return false;
        }
        return this.store.containsKey(new CustomerId(customerId));
    }
}
