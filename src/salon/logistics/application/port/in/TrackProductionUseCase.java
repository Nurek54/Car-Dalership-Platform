package salon.logistics.application.port.in;

import java.time.LocalDate;

/**
 * Port wejściowy: śledzenie produkcji w fabryce (UC-INW-04 wg architektury) —
 * węzeł "TrackProductionUseCase" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 */
public interface TrackProductionUseCase {

    /** Odpytuje fabrykę o status slotu (FactoryStatusAclPort) i aktualizuje agregat ProductionSlot. */
    void updateProductionStatus(String slotId, LocalDate estimatedDelivery);
}
