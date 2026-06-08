package salon.billing.domain.model.settlement;

import salon.shared.model.Money;

import java.time.LocalDateTime;

/**
 * Encja LOKALNA wewnątrz agregatu {@link Settlement}.
 *
 * Pojedyncza transakcja z wyciągu bankowego NIE posiada globalnej tożsamości ani własnego
 * repozytorium — jest enkapsulowana i zapisywana jako element kolekcji wewnątrz agregatu Settlement.
 * Gwarantuje to, że wpłata nie może istnieć w oderwaniu od przypisanego do niej zamówienia.
 */
public class Payment {

    private final String transactionId;
    private final Money amount;
    private final LocalDateTime paymentDate;

    public Payment(String transactionId, Money amount, LocalDateTime paymentDate) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be blank.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null.");
        }
        if (paymentDate == null) {
            throw new IllegalArgumentException("paymentDate must not be null.");
        }
        this.transactionId = transactionId;
        this.amount = amount;
        this.paymentDate = paymentDate;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public Money getAmount() {
        return this.amount;
    }

    public LocalDateTime getPaymentDate() {
        return this.paymentDate;
    }
}
