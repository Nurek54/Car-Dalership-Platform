package salon.financing.domain.model.financing;

import java.math.BigDecimal;

/**
 * Value Object: decyzja banku przetłumaczona przez ACL (czysta, niemutowalna).
 * Dostarczana z zewnątrz do agregatu — jego stan nie zmienia się bez zweryfikowanej decyzji.
 */
public record FinancingDecision(String bankReference, BigDecimal grantedAmount, DecisionStatus status) {

    public FinancingDecision {
        if (bankReference == null || bankReference.isBlank()) {
            throw new IllegalArgumentException("bankReference must not be blank.");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null.");
        }
        if (status == DecisionStatus.APPROVED) {
            if (grantedAmount == null || grantedAmount.signum() <= 0) {
                throw new IllegalArgumentException("An approved decision requires a positive grantedAmount.");
            }
        }
    }
}
