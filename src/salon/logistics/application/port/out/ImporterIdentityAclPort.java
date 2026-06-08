package salon.logistics.application.port.out;

import salon.logistics.domain.model.vehicle.ImporterData;

/**
 * Port wyjściowy (ACL): weryfikacja tożsamości pojazdu w systemie Importera (UC-INW-01).
 * Tłumaczy dane zewnętrzne na czysty obiekt domenowy ImporterData.
 */
public interface ImporterIdentityAclPort {
    ImporterData verifyVin(String vin);
}
