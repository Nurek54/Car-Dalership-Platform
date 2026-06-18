package salon.billing.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountingDocumentJpaRepository extends JpaRepository<AccountingDocumentJpaEntity, String> {

    List<AccountingDocumentJpaEntity> findByOrderId(String orderId);
}
