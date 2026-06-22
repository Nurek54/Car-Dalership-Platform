package salon.sales.application.port.out;

import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;

import java.util.Optional;

/**
 * OUTBOUND PORT (Figure 22) — "CustomerDatabaseRepository". Persistence of the Customer aggregate.
 */
public interface CustomerDatabaseRepository {

    void save(Customer customer);

    Optional<Customer> findById(CustomerId id);

    /** UC-CRM-01: quick existence check used before opening a configurator session. */
    boolean existsById(String customerId);
}
