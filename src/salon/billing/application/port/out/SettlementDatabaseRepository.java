package salon.billing.application.port.out;

import salon.billing.application.domain.model.settlement.Settlement;
import salon.common.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * PORT WYJSCIOWY (Rys. 48 — SettlementDatabaseRepository) — utrwalanie agregatu Rozliczenia.
 *
 * Repozytorium udostepnia agregaty jednego typu (Settlement), tworzac zludzenie kolekcji w pamieci.
 * Indeks po OrderId, bo wplaty i zadania faktur odnosza sie do zamowienia. Nie kontroluje transakcji
 * (to usluga aplikacji) i nie tworzy agregatow (to fabryka).
 */
public interface SettlementDatabaseRepository {

    void save(Settlement settlement);

    Optional<Settlement> findByOrderId(OrderId orderId);

    List<Settlement> findAll();
}
