package salon.sales.infrastructure.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    public String id;
    public String sourceOfferId;
    public String specificationId;
    // Kwoty trzymane tekstowo (toPlainString) — wierny round-trip BigDecimal bez zmiany skali.
    public String requiredDepositAmount;
    public String requiredDepositCurrency;
    public String paymentMethod;
    public String paymentStatus;
    public LocalDate handoverDate;
    public String vehicleId;
    public String state;

    /** Blokada optymistyczna: konflikt wersji odrzuca zapis nieaktualnej kopii agregatu. */
    @Version
    public Long version;

    public OrderJpaEntity() {
    }
}
