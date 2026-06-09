package salon.billing.infrastructure.persistence;

import salon.billing.infrastructure.persistence.AccountingDocumentJpaEntity;
import salon.billing.infrastructure.persistence.AccountingDocumentJpaRepository;

import salon.shared.infrastructure.persistence.DomainReflection;
import org.springframework.stereotype.Component;
import salon.billing.application.port.out.DocumentRepository;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.BuyerDetails;
import salon.billing.domain.model.document.DocumentId;
import salon.billing.domain.model.document.DocumentStatus;
import salon.billing.domain.model.document.SellerDetails;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Adapter sterowany (driven) — persystencja agregatu AccountingDocument (port {@link DocumentRepository}).
 *
 * Agregat jest niemutowalny i powstaje wyłącznie przez fabrykę {@code createInvoice} (ustawia "teraz"
 * jako daty). Aby wiernie odtworzyć ZAPISANE daty/status bez modyfikacji domeny, używamy refleksji
 * w warstwie infrastruktury (DomainReflection) — kod kontekstu Rozliczeń pozostaje nietknięty.
 */
@Component
public class AccountingDocumentDatabaseAdapter implements DocumentRepository {

    private static final Class<?>[] CTOR_TYPES = new Class<?>[]{
            DocumentId.class, OrderId.class, String.class, BuyerDetails.class, SellerDetails.class,
            Money.class, LocalDate.class, LocalDate.class, String.class};

    private final AccountingDocumentJpaRepository repository;

    public AccountingDocumentDatabaseAdapter(AccountingDocumentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(AccountingDocument document) {
        repository.save(toEntity(document));
    }

    @Override
    public Optional<AccountingDocument> findById(DocumentId id) {
        return repository.findById(id.value()).map(this::toDomain);
    }

    private AccountingDocumentJpaEntity toEntity(AccountingDocument document) {
        AccountingDocumentJpaEntity entity = new AccountingDocumentJpaEntity();
        entity.id = document.getId().value();
        entity.orderId = document.getOrderId().value();
        entity.invoiceTitle = document.getInvoiceTitle();
        entity.buyerName = document.getBuyer().name();
        entity.buyerNip = document.getBuyer().nip();
        entity.sellerName = document.getSeller().name();
        entity.sellerNip = document.getSeller().nip();
        entity.amount = document.getTotalAmount().amount();
        entity.currency = document.getTotalAmount().currency();
        entity.issueDate = document.getIssueDate();
        entity.dueDate = document.getDueDate();
        entity.authorizedIssuer = document.getAuthorizedIssuer();
        entity.status = document.getStatus().name();
        return entity;
    }

    private AccountingDocument toDomain(AccountingDocumentJpaEntity entity) {
        Object[] args = new Object[]{
                new DocumentId(entity.id),
                new OrderId(entity.orderId),
                entity.invoiceTitle,
                new BuyerDetails(entity.buyerName, entity.buyerNip),
                new SellerDetails(entity.sellerName, entity.sellerNip),
                Money.of(entity.amount, entity.currency),
                entity.issueDate,
                entity.dueDate,
                entity.authorizedIssuer};
        AccountingDocument document = DomainReflection.instantiate(AccountingDocument.class, CTOR_TYPES, args);
        DomainReflection.set(document, "status", DocumentStatus.valueOf(entity.status));
        return document;
    }
}
