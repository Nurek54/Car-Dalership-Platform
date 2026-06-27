package salon.sales.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import salon.sales.application.domain.model.order.OrderState;
import salon.sales.application.domain.model.order.PaymentMethod;
import salon.sales.application.domain.model.order.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sales_order")
public class OrderEntity {

    @Id
    private String id;
    private String sourceOfferId;
    private BigDecimal depositAmount;
    private String depositCurrency;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private LocalDate handoverDate;
    @Enumerated(EnumType.STRING)
    private OrderState state;
    @Version
    private Long version;

    protected OrderEntity() {
    }

    public OrderEntity(String id, String sourceOfferId, BigDecimal depositAmount, String depositCurrency,
                       PaymentMethod paymentMethod, PaymentStatus paymentStatus, LocalDate handoverDate,
                       OrderState state, Long version) {
        this.id = id;
        this.sourceOfferId = sourceOfferId;
        this.depositAmount = depositAmount;
        this.depositCurrency = depositCurrency;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.handoverDate = handoverDate;
        this.state = state;
        this.version = version;
    }

    public String getId() { return id; }
    public String getSourceOfferId() { return sourceOfferId; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public String getDepositCurrency() { return depositCurrency; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public LocalDate getHandoverDate() { return handoverDate; }
    public OrderState getState() { return state; }
    public Long getVersion() { return version; }
}
