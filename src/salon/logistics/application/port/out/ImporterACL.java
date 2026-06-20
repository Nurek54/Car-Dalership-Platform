package salon.logistics.application.port.out;

import salon.logistics.application.domain.exception.FactoryOrderFailedException;

import java.util.List;

/**
 * PORT WYJŚCIOWY (Rysunek 37) – „ImporterACL”.
 *
 * Warstwa zapobiegająca uszkodzeniu (ACL) integracji z systemem fabryki/importera:
 * adapter tłumaczy lokalne żądanie produkcji na format zewnętrznego API i zwraca nadany
 * numer VIN. Niepowodzenie zgłasza jako {@link FactoryOrderFailedException} (UC-INW-02 / A1).
 */
public interface ImporterACL {

    /**
     * Składa zlecenie produkcji i zwraca nadany przez fabrykę numer VIN.
     *
     * @throws FactoryOrderFailedException gdy fabryka odrzuci zlecenie / wystąpi błąd integracji
     */
    String placeFactoryOrder(String orderId, List<String> optionCodes);
}
