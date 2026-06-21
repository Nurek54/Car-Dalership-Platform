package salon.financing.application.port.out;

import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.OrderId;

import java.util.Optional;

/**
 * PORT WYJSCIOWY (Rysunek 42 — DBAdapter) — utrwalanie agregatu Wniosku o finansowanie.
 *
 * Repozytorium udostepnia agregaty jednego typu (FinancingApplication). Indeks po identyfikatorze
 * zamowienia, bo decyzja banku (UC-FIN-02) odnosi sie do zamowienia. Nie kontroluje transakcji
 * (to usluga aplikacji) i nie tworzy agregatow (to fabryka).
 */
public interface FinancingApplicationDatabaseRepository {

    void save(FinancingApplication application);

    Optional<FinancingApplication> findByOrderId(OrderId orderId);
}
