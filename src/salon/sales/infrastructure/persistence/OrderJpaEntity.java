package salon.sales.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Encja JPA zamówienia (UC-SPR-02, UC-SPR-03). */
@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    public String id;
    public String sourceOfferId;
    public BigDecimal requiredDepositAmount;
    public String requiredDepositCurrency;
    public String signatureRef;
    public String state;
    public String cancellationReason;

    public OrderJpaEntity() {
    }
}
