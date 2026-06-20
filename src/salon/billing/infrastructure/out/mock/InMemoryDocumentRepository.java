package salon.billing.infrastructure.out.mock;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.domain.model.document.DocumentId;
import salon.billing.application.port.out.DocumentDatabaseRepository;
import salon.common.model.OrderId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ADAPTER WYJSCIOWY (Rys. 48 — DBAdapter) — implementacja {@link DocumentDatabaseRepository}
 * w pamieci. Indeks glowny po DocumentId; wyszukiwanie po zamowieniu przeglada wartosci.
 */
public class InMemoryDocumentRepository implements DocumentDatabaseRepository {

    private final Map<String, AccountingDocument> byId = new ConcurrentHashMap<>();

    @Override
    public void save(AccountingDocument document) {
        this.byId.put(document.getId().value(), document);
    }

    @Override
    public Optional<AccountingDocument> findById(DocumentId id) {
        return Optional.ofNullable(this.byId.get(id.value()));
    }

    @Override
    public List<AccountingDocument> findByOrderId(OrderId orderId) {
        List<AccountingDocument> result = new ArrayList<>();
        for (AccountingDocument document : this.byId.values()) {
            if (document.getOrderId().value().equals(orderId.value())) {
                result.add(document);
            }
        }
        return result;
    }
}
