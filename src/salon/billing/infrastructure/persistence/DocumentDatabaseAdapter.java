package salon.billing.infrastructure.persistence;

import salon.billing.application.port.out.DocumentRepository;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.DocumentId;

import java.util.Optional;

public class DocumentDatabaseAdapter implements DocumentRepository {

    @Override
    public void save(AccountingDocument document) {
        throw new UnsupportedOperationException("TODO: implement JPA persistence for AccountingDocument.");
    }

    @Override
    public Optional<AccountingDocument> findById(DocumentId id) {
        throw new UnsupportedOperationException("TODO: implement JPA lookup for AccountingDocument.");
    }
}
