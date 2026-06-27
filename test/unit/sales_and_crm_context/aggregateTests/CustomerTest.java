package unit.sales_and_crm_context.aggregateTests;

import org.junit.jupiter.api.Test;
import salon.sales.application.domain.exception.InvalidTaxIdException;
import salon.sales.application.domain.model.customer.Address;
import salon.sales.application.domain.model.customer.ContactData;
import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.customer.CustomerId;

import static org.assertj.core.api.Assertions.*;

class CustomerTest {

    @Test
    void shouldSuccessfullyVerifyValidTaxId() {
        // Klient z poprawnym, 10-cyfrowym polskim NIP-em
        Customer customer = new Customer(
                new CustomerId("C-001"),
                "Jan Kowalski",
                "1234563218",
                new Address("Warszawa", "00-001", "Testowa 1", "Poland"),
                new ContactData("jan@example.com", "123456789")
        );

        // Weryfikacja nie rzuca wyjątku, status weryfikacji jest poprawny
        assertThatCode(() -> customer.verifyTaxId())
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenTaxIdIsInvalid() {
        // Klient z niepoprawnym NIP-em
        Customer customer = new Customer(
                new CustomerId("C-002"),
                "Jan Krzak",
                "INVALID_NIP_123",
                new Address("Krakow", "30-001", "Fictional 2", "Poland"),
                new ContactData("krzak@example.com", "987654321")
        );

        // Metoda verifyTaxId blokuje proces biznesowy
        assertThatThrownBy(() -> customer.verifyTaxId())
                .isInstanceOf(InvalidTaxIdException.class)
                .hasMessageContaining("Provided NIP format is invalid");
    }

    @Test
    void shouldUpdateContactDetailsWithoutChangingIdentity() {
        // Istniejący klient
        CustomerId id = new CustomerId("C-003");
        Customer customer = new Customer(
                id,
                "Anna Nowak",
                null, // Osoba fizyczna, brak NIP-u
                new Address("Poznan", "60-001", "Old 1", "Poland"),
                new ContactData("anna@example.com", "111222333")
        );

        // Zmiana adresu e-mail i numeru telefonu
        ContactData newContact = new ContactData("nowa.anna@example.com", "999888777");
        customer.updateContactDetails(newContact);

        // Dane kontaktowe są zaktualizowane, ale tożsamość (ID) pozostaje nienaruszona
        assertThat(customer.contact().email()).isEqualTo("nowa.anna@example.com");
        assertThat(customer.contact().phoneNumber()).isEqualTo("999888777");
        assertThat(customer.id()).isEqualTo(id);
    }
}
