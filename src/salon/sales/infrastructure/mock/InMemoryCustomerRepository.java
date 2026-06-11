package salon.sales.infrastructure.mock;

import org.springframework.stereotype.Component;
import salon.sales.application.port.out.CustomerRepository;
import salon.sales.domain.model.customer.Customer;
import salon.sales.domain.model.customer.CustomerId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter wyjściowy (in-memory) portu CustomerRepository — do dem i uruchomień bez bazy.
 * Klient nie ma jeszcze adaptera JPA, więc rejestr działa w pamięci procesu.
 */
@Component
public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<CustomerId, Customer> store = new HashMap<>();

    @Override
    public void save(Customer customer) {
        this.store.put(customer.getId(), customer);
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
