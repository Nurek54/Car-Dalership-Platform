package salon.billing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountingDocumentJpaRepository extends JpaRepository<AccountingDocumentJpaEntity, String> {
}
