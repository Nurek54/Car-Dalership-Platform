package salon.sales.application.port.out;

import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;

import java.util.Optional;

// Port wyjściowy: repozytorium agregatu Klient (CRM).
public interface CustomerDatabaseRepository {

    void save(Customer customer);

    Optional<Customer> findById(CustomerId id);

    /** UC-CRM-01: szybka weryfikacja, czy klient istnieje w bazie CRM. */
    boolean existsById(String customerId);
}
