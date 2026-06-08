package salon.financing.domain.model.financing;

/**
 * Status decyzji instytucji finansowej (po przejściu przez ACL).
 * ZALOZENIE: wartości nie wynikają wprost z PDF — przyjęto APPROVED/REJECTED.
 */
public enum DecisionStatus {
    APPROVED,
    REJECTED
}
