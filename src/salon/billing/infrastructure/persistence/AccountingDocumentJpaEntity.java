package salon.billing.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Encja JPA dokumentu księgowego (faktura/dokument zadatku, UC-FIR-02). */
@Entity
@Table(name = "accounting_documents")
public class AccountingDocumentJpaEntity {

    @Id
    public String id;
    public String orderId;
    public String invoiceTitle;
    public String buyerName;
    public String buyerNip;
    public String sellerName;
    public String sellerNip;
    public BigDecimal amount;
    public String currency;
    public LocalDate issueDate;
    public LocalDate dueDate;
    public String authorizedIssuer;
    public String status;

    public AccountingDocumentJpaEntity() {
    }
}
