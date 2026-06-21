package salon.sales.infrastructure.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "offers")
public class OfferJpaEntity {

    @Id
    public String id;
    public String customerId;
    public String specificationId;
    // Kwoty trzymane tekstowo (toPlainString) — wierny round-trip BigDecimal bez zmiany skali.
    public String basePriceAmount;
    public String basePriceCurrency;
    public BigDecimal discountPercentage;
    public String finalPriceAmount;
    public String finalPriceCurrency;
    public LocalDate validityDate;
    public String state;

    /** Optimistic locking: a version conflict rejects saving a stale copy of the aggregate. */
    @Version
    public Long version;

    public OfferJpaEntity() {
    }
}
