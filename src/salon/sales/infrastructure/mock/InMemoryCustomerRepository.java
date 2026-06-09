package salon.sales.infrastructure.mock;

import salon.sales.application.port.out.CustomerRepository;
import salon.sales.domain.model.customer.Customer;
import salon.sales.domain.model.customer.CustomerId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
}
