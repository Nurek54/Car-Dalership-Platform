package salon.logistics.application.port.out;

import salon.shared.model.OrderId;

import java.util.List;

/**
 * Port wyjściowy (driven, ACL) integracji z Kontekstem Katalogu/Sprzedaży.
 *
 * Agregat InventoryVehicle zna tylko VIN i OrderId — nie wie, czy ma zamówić czerwonego
 * sedana, czy czarne kombi. Zanim Inwentarz wyśle zlecenie produkcyjne do fabryki
 * (UC-INW-02), musi pozyskać kody wyposażenia (silnik, kolor, opcje) zatwierdzonej
 * specyfikacji dla danego zamówienia. Źródłem prawdy jest Katalog/Sprzedaż — ten port
 * pozwala je dociągnąć, gdy zdarzenie wyzwalające ich nie niesie.
 *
 * Konkretną komunikację (REST/komunikat) realizuje adapter w warstwie infrastruktury.
 */
public interface SpecificationIntegrationPort {

    /**
     * Zwraca kody wyposażenia zatwierdzonej specyfikacji dla danego zamówienia.
     */
    List<String> getSpecificationForOrder(OrderId orderId);
}
