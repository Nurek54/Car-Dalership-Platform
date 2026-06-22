package salon.sales.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import salon.sales.application.domain.model.offer.OfferState;

import java.math.BigDecimal;
import java.time.LocalDate;

/** JPA persistence model for the {@link salon.sales.application.domain.model.offer.Offer} aggregate. */
@Entity
@Table(name = "sales_offer")
public class OfferEntity {

    @Id
    private String id;
    private String customerId;
    private String specificationId;
    private BigDecimal baseAmount;
    private String baseCurrency;
    private BigDecimal finalAmount;
    private String finalCurrency;
    @Enumerated(EnumType.STRING)
    private OfferState state;
    private LocalDate validityDate;
    @Version
    private Long version;

    protected OfferEntity() {
    }

    public OfferEntity(String id, String customerId, String specificationId,
                       BigDecimal baseAmount, String baseCurrency,
                       BigDecimal finalAmount, String finalCurrency,
                       OfferState state, LocalDate validityDate, Long version) {
        this.id = id;
        this.customerId = customerId;
        this.specificationId = specificationId;
        this.baseAmount = baseAmount;
        this.baseCurrency = baseCurrency;
        this.finalAmount = finalAmount;
        this.finalCurrency = finalCurrency;
        this.state = state;
        this.validityDate = validityDate;
        this.version = version;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getSpecificationId() { return specificationId; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public String getBaseCurrency() { return baseCurrency; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public String getFinalCurrency() { return finalCurrency; }
    public OfferState getState() { return state; }
    public LocalDate getValidityDate() { return validityDate; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
