package salon.billing.application.port.out;

import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.DocumentId;

import java.util.Optional;

public interface DocumentRepository {
    void save(AccountingDocument document);
    Optional<AccountingDocument> findById(DocumentId id);
}
