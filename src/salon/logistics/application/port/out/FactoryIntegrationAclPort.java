package salon.logistics.application.port.out;

import salon.logistics.domain.model.vehicle.ImporterData;
import salon.logistics.domain.model.vehicle.VinNumber;
import salon.shared.model.OrderId;

import java.util.List;

/**
 * Port wyjściowy (driven, ACL) dwukierunkowej komunikacji z API producenta/importera —
 * węzeł "FactoryIntegrationAclPort" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md
 * (PDF rozdz. 3.5.3 "Wzorzec ACL i Fabryka dla Produkcji").
 *
 * Warstwa tłumacząca izoluje jądro systemu od formatu danych fabryki: wysyła komendę
 * PlaceFactoryOrder i tłumaczy odpowiedź (FactoryOrderAcknowledged / FactoryOrderFailed)
 * na czyste typy domenowe.
 */
public interface FactoryIntegrationAclPort {

    /**
     * UC-INW-02, krok 3: wysłanie zlecenia produkcji (komenda PlaceFactoryOrder).
     * Zwraca numer VIN przydzielony przez fabrykę (FactoryOrderAcknowledged).
     *
     * @throws FactoryOrderRejectedException gdy API fabryki odrzuca zlecenie (A1).
     */
    VinNumber placeFactoryOrder(OrderId orderId, List<String> specCodes);

    /** UC-INW-03: weryfikacja tożsamości pojazdu w bazie importera (dane do przyjęcia na plac). */
    ImporterData fetchVehicleData(VinNumber vin);
}
