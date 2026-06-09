package salon.billing.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Encja JPA agregatu rozliczenia. Infrastruktura — domena (salon.*.domain) pozostaje czysta. */
@Entity
@Table(name = "settlements")
public class SettlementJpaEntity {

    @Id
    public String id;
    public String orderId;
    public BigDecimal totalAmount;
    public String currency;
    public String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "settlement_payments", joinColumns = @JoinColumn(name = "settlement_id"))
    public List<PaymentEmbeddable> payments = new ArrayList<>();

    public SettlementJpaEntity() {
    }
}
