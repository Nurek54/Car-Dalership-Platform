package salon.sales.application.port.out;

import salon.sales.domain.model.customer.Customer;
import salon.sales.domain.model.customer.CustomerId;

import java.util.Optional;

/**
 * Port wyjściowy (driven) dla agregatu Customer — patrz węzeł
 * "Repositories (Customer, Offer, Order)" w docs/Architecture/SalesArchitecture.md.
 */
public interface CustomerRepository {
    void save(Customer customer);
    Optional<Customer> findById(CustomerId id);
}
