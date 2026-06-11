package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.DocumentRepository;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.DocumentId;
import salon.shared.model.OrderId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Override
    public List<AccountingDocument> findByOrderId(OrderId orderId) {
        List<AccountingDocument> result = new ArrayList<>();
        for (AccountingDocument document : this.store.values()) {
            if (document.getOrderId().equals(orderId)) {
                result.add(document);
            }
        }
        return result;
    }

    @Override
    public List<AccountingDocument> findAll() {
        return new ArrayList<>(this.store.values());
    }
}
