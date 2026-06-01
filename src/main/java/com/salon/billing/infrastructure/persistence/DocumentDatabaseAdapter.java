package main.java.com.salon.billing.infrastructure.persistence;

import main.java.com.salon.billing.application.port.out.DocumentRepository;
import main.java.com.salon.billing.domain.model.document.AccountingDocument;
import main.java.com.salon.billing.domain.model.document.DocumentId;

import java.util.Optional;

/**
 * Adapter bazodanowy (DatabaseAdapter) dla agregatu AccountingDocument — SZKIELET pod ORM (JPA).
 * Uwaga: dokument ma pozycje (DocumentLine) — w JPA odwzorujemy je relacją @OneToMany
 * na osobnych encjach JPA, a mapper złoży/rozbije agregat. Domena pozostaje czysta.
 */
public class DocumentDatabaseAdapter implements DocumentRepository {

    // TODO: private final DocumentJpaRepository jpaRepository;
    // TODO: private final DocumentMapper mapper;

    @Override
    public void save(AccountingDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null.");
        }
        // TODO: jpaRepository.save(mapper.toEntity(document));
        throw new UnsupportedOperationException("DocumentDatabaseAdapter.save: not implemented yet (JPA skeleton).");
    }

    @Override
    public Optional<AccountingDocument> findById(DocumentId id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        // TODO: return jpaRepository.findById(id.value()).map(mapper::toDomain);
        throw new UnsupportedOperationException("DocumentDatabaseAdapter.findById: not implemented yet (JPA skeleton).");
    }
}
