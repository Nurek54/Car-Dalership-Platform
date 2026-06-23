package salon.sales.application.port.out;

import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;

import java.util.Optional;

public interface CustomerDatabaseRepository {

    void save(Customer customer);

    Optional<Customer> findById(CustomerId id);

    
    boolean existsById(String customerId);
}
