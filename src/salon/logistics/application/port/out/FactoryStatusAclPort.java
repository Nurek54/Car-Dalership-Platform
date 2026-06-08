package salon.logistics.application.port.out;

import salon.logistics.domain.model.slot.FactoryStatus;

/**
 * Port wyjściowy (ACL): odpytanie statusu produkcji w fabryce (UC-INW-04).
 */
public interface FactoryStatusAclPort {
    FactoryStatus fetchStatus(String factoryJobId);
}
