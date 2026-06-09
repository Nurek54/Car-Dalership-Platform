package salon.sales.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Encja JPA oferty handlowej (UC-SPR-01). */
@Entity
@Table(name = "offers")
public class OfferJpaEntity {

    @Id
    public String id;
    public String customerId;
    public String specificationId;
    public BigDecimal basePriceAmount;
    public String basePriceCurrency;
    public BigDecimal discountPercentage;
    public BigDecimal finalPriceAmount;
    public String finalPriceCurrency;
    public LocalDate validityDate;
    public String state;

    public OfferJpaEntity() {
    }
}
