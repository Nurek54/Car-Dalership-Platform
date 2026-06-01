package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.DocumentRepository;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.DocumentId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryDocumentRepository implements DocumentRepository {

    private final Map<DocumentId, AccountingDocument> store = new HashMap<>();

    @Override
    public void save(AccountingDocument document) {
        this.store.put(document.getId(), document);
    }

    @Override
    public Optional<AccountingDocument> findById(DocumentId id) {
        return Optional.ofNullable(this.store.get(id));
    }
}
