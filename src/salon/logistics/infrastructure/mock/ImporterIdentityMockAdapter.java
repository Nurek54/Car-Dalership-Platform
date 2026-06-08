package salon.logistics.infrastructure.mock;

import salon.logistics.application.port.out.ImporterIdentityAclPort;
import salon.logistics.domain.model.vehicle.ImporterData;

/**
 * Udawany ACL Importera — "potwierdza" każdy VIN, zwracając czysty ImporterData.
 */
public class ImporterIdentityMockAdapter implements ImporterIdentityAclPort {

    @Override
    public ImporterData verifyVin(String vin) {
        return new ImporterData(vin);
    }
}
