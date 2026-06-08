package integration.driven_adapters.database_adapter.billing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.billing.domain.model.Payment;
import salon.billing.domain.model.PaymentId;
import salon.billing.domain.model.PaymentCategory;
import salon.billing.infrastructure.persistence.PaymentDatabaseAdapter;
import salon.shared.model.OrderId;
import salon.shared.model.Money;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PaymentDatabaseAdapter.class)
class PaymentDatabaseAdapterTest {

    @Autowired
    private PaymentDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: Zagnieżdżone obiekty i Enumy
    @Test
    void shouldSaveAndRetrievePaymentWithMoneyAndCategoryMapped() {
        // Arrange
        PaymentId paymentId = new PaymentId("PAY-2026-999");
        OrderId orderId = new OrderId("ORD-555");
        Money amount = Money.of(new BigDecimal("5000.00"), "PLN");
        Money requiredDeposit = Money.of(new BigDecimal("5000.00"), "PLN");

        Payment payment = new Payment(paymentId, orderId, amount);

        // Aktywujemy logikę dziedziny - skoro wpłata pokrywa wymagany zadatek,
        // to jej kategoria powinna zostać ustawiona na DEPOSIT
        payment.categorizePayment(requiredDeposit);

        // Act - Adapter transformuje i zapisuje agregat
        adapter.save(payment);

        // Odczyt z bazy (wymuszenie mapowania JPA -> Obiekt Dziedziny)
        Optional<Payment> retrievedPayment = adapter.findById(paymentId);

        // Assert
        assertThat(retrievedPayment).isPresent();
        Payment retrieved = retrievedPayment.get();

        assertThat(retrieved.getId()).isEqualTo(paymentId);
        assertThat(retrieved.getOrderId()).isEqualTo(orderId);

        // Krytyczna asercja finansowa: Weryfikacja typu @Embedded w Hibernate dla obiektu Money
        assertThat(retrieved.getAmount().getAmount()).isEqualByComparingTo("5000.00");
        assertThat(retrieved.getAmount().getCurrency()).isEqualTo("PLN");

        // Sprawdzenie, czy kategoria poprawnie zapisała się w bazie (np. jako String "DEPOSIT")
        assertThat(retrieved.getCategory()).isEqualTo(PaymentCategory.DEPOSIT);
    }

    // 2. BRAK DANYCH
    @Test
    void shouldReturnEmptyOptionalWhenPaymentDoesNotExist() {
        // Act
        Optional<Payment> result = adapter.findById(new PaymentId("PAY-UNKNOWN"));

        // Assert
        assertThat(result).isEmpty();
    }
}