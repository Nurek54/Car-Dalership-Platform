package salon.logistics.domain.model.slot;

/**
 * Status z systemu fabryki/Importera (po przejściu przez ACL).
 * ZALOZENIE: wartości nie wynikają wprost z PDF — przyjęto typowy cykl produkcyjny.
 */
public enum FactoryStatus {
    REGISTERED,
    IN_PRODUCTION,
    SHIPPED,
    DELIVERED
}
