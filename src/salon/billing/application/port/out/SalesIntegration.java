package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.BuyerDetails;
import salon.common.model.OrderId;

/**
 * Port wyjściowy (driven, ACL) integracji z Kontekstem Sprzedaży/CRM.
 *
 * Zdarzenia przychodzące z Inwentarza (np. VehicleReservedFromStock) niosą wyłącznie
 * techniczne identyfikatory (orderId, VIN) — nie zawierają imienia, nazwiska, NIP-u ani
 * adresu klienta. Żeby wystawić legalną fakturę (UC-FIR-01/UC-FIR-02), DocumentGenerationService
 * musi dociągnąć dane nabywcy z modułu Sprzedaży/CRM przez ten port, zamiast oczekiwać,
 * że pojawią się "z powietrza" w komendzie.
 *
 * Konkretną komunikację (REST/komunikat) realizuje adapter w warstwie infrastruktury.
 */
public interface SalesIntegration {

    /**
     * Zwraca dane nabywcy (imię i nazwisko / nazwa firmy, NIP) dla danego zamówienia.
     */
    BuyerDetails getCustomerDetails(OrderId orderId);
}
