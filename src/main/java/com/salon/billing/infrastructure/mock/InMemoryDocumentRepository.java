package main.java.com.salon.billing.infrastructure.mock;

import main.java.com.salon.billing.application.port.out.DocumentRepository;
import main.java.com.salon.billing.domain.model.document.AccountingDocument;
import main.java.com.salon.billing.domain.model.document.DocumentId;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repozytorium AccountingDocument — MOCK w pamięci (patrz InMemoryPaymentRepository).
 */
public class InMemoryDocumentRepository implements DocumentRepository {

    private final Map<UUID, AccountingDocument> store = new ConcurrentHashMap<>();

    @Override
    public void save(AccountingDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null.");
        }
        store.put(document.getId().value(), document);
    }

    @Override
    public Optional<AccountingDocument> findById(DocumentId id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        return Optional.ofNullable(store.get(id.value()));
    }
}
