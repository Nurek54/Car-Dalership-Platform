package salon.sales.application.port.out;

import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;

import java.util.Optional;

// Outbound port: repository of the Customer aggregate (CRM).
public interface CustomerDatabaseRepository {

    void save(Customer customer);

    Optional<Customer> findById(CustomerId id);

    /** UC-CRM-01: a quick check whether the customer exists in the CRM database. */
    boolean existsById(String customerId);
}
