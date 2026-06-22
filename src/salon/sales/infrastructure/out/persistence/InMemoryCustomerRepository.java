package salon.sales.infrastructure.out.persistence;

import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.port.out.CustomerDatabaseRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCustomerRepository implements CustomerDatabaseRepository {

    private final Map<String, Customer> byId = new ConcurrentHashMap<>();

    @Override
    public void save(Customer customer) {
        this.byId.put(customer.getId().value(), customer);
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return Optional.ofNullable(this.byId.get(id.value()));
    }
}
