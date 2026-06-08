package salon.logistics.infrastructure.mock;

import salon.logistics.application.port.out.FactoryStatusAclPort;
import salon.logistics.domain.model.slot.FactoryStatus;

/**
 * Udawany ACL fabryki — zawsze zwraca "w produkcji".
 */
public class FactoryStatusMockAdapter implements FactoryStatusAclPort {

    @Override
    public FactoryStatus fetchStatus(String factoryJobId) {
        return FactoryStatus.IN_PRODUCTION;
    }
}
