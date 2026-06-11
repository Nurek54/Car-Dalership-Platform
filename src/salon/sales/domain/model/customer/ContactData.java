package salon.sales.domain.model.customer;

/** Obiekt wartości: dane kontaktowe klienta (e-mail + telefon). Niemutowalny. */
public record ContactData(String email, String phone) {

    public ContactData {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank.");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("phone must not be blank.");
        }
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhoneNumber() {
        return this.phone;
    }
}
