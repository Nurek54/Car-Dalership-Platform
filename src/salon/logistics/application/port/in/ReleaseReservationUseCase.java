package salon.logistics.application.port.in;

/**
 * Port wejściowy UC-INW-04: zwolnienie blokady pojazdu —
 * węzeł "ReleaseReservationUseCase" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 *
 * Wyzwalany zdarzeniem PaymentDeadlineExpired z Kontekstu Fakturowania (kontekst nie ma
 * własnego mechanizmu odliczania czasu — patrz Assumptions na kanwie). Operacja jest
 * idempotentna (A1: brak pojazdu w rezerwacjach -> brak akcji).
 */
public interface ReleaseReservationUseCase {

    void releaseReservationForOrder(String orderId);
}
