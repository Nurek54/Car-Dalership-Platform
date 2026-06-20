package salon.logistics.application.domain.model.vehicle;

import salon.common.model.OrderId;
import salon.logistics.application.domain.exception.IllegalVehicleStateException;

/**
 * KORZEŃ AGREGATU (Rysunek 38) – fizyczny egzemplarz pojazdu w Inwentarzu.
 *
 * Agregat strzeże niezmienników cyklu życia pojazdu (maszyna stanów) i jest jedynym
 * miejscem zmiany statusu oraz powiązania z zamówieniem. Odwołanie do zamówienia jest
 * ROZŁĄCZNE (przez {@link OrderId} ze wspólnego rdzenia) — Inwentarz nie zna agregatu Order.
 *
 * Logika biznesowa (przejścia stanów) jest tutaj; orkiestracja i zdarzenia — w usłudze aplikacji.
 */
public class InventoryVehicle {

    private final VinNumber vin;
    private final SpecificationId specification; // może być null dla aut "na stock" bez specyfikacji
    private VehicleRole role;
    private VehicleState state;
    private OrderId order; // 0..1 — przypisane dopiero po rezerwacji/zleceniu

    /** Konstruktor pakietowy — egzemplarze tworzy wyłącznie {@link InventoryVehicleFactory}. */
    InventoryVehicle(VinNumber vin, SpecificationId specification,
                     VehicleRole role, VehicleState state, OrderId order) {
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        if (role == null) {
            throw new IllegalArgumentException("role must not be null.");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must not be null.");
        }
        this.vin = vin;
        this.specification = specification;
        this.role = role;
        this.state = state;
        this.order = order;
    }

    /** UC-INW-01, krok 3–4: twarda blokada wolnego pojazdu z placu dla zamówienia. */
    public void lockForOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (this.state != VehicleState.ON_STOCK) {
            throw new IllegalVehicleStateException(
                    "Zarezerwować można tylko pojazd ON_STOCK (aktualny: " + this.state + ").");
        }
        this.order = orderId;
        this.state = VehicleState.RESERVED;
    }

    /**
     * UC-INW-03 (Rysunek 38: {@code receiveOnYard(ImporterData)}): zjazd z lawety pojazdu
     * zamówionego w fabryce. Dane importera (skan VIN) są reconciliowane z kartoteką, po czym
     * pojazd zostaje sparowany z zamówieniem (RESERVED).
     */
    public void receiveOnYard(ImporterData data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null.");
        }
        if (!this.vin.equals(data.vin())) {
            throw new IllegalVehicleStateException(
                    "Zeskanowany VIN " + data.vin() + " nie zgadza się z kartoteką pojazdu " + this.vin + ".");
        }
        if (this.state != VehicleState.IN_PRODUCTION) {
            throw new IllegalVehicleStateException(
                    "Na plac (z parowaniem do zamówienia) można przyjąć tylko pojazd IN_PRODUCTION (aktualny: "
                            + this.state + ").");
        }
        this.state = VehicleState.RESERVED;
    }

    /** UC-INW-04, krok 3: automatyczne zdjęcie blokady — pojazd wraca na plac jako wolny. */
    public void releaseReservation() {
        if (this.state != VehicleState.RESERVED) {
            throw new IllegalVehicleStateException(
                    "Zwolnić można tylko rezerwację pojazdu RESERVED (aktualny: " + this.state + ").");
        }
        this.order = null;
        this.state = VehicleState.ON_STOCK;
    }

    /**
     * UC-INW-05, krok 3: po rozliczeniu salda pojazd staje się gotowy do wydania.
     * (Metoda spoza minimalnego zestawu z Rysunku 38 — wymagana przez UC-INW-05.)
     */
    public void prepareForHandover() {
        if (this.state != VehicleState.RESERVED) {
            throw new IllegalVehicleStateException(
                    "Do wydania można przygotować tylko pojazd RESERVED (aktualny: " + this.state + ").");
        }
        this.state = VehicleState.READY_FOR_HANDOVER;
    }

    /**
     * UC-INW-06, krok 3: fizyczne wydanie — zdjęcie z aktywnego stanu magazynowego.
     * (Metoda spoza minimalnego zestawu z Rysunku 38 — wymagana przez UC-INW-06.)
     */
    public void handOver() {
        if (this.state != VehicleState.READY_FOR_HANDOVER) {
            throw new IllegalVehicleStateException(
                    "Wydać można tylko pojazd READY_FOR_HANDOVER (aktualny: " + this.state + ").");
        }
        this.state = VehicleState.HANDED_OVER;
    }

    /** Oznaczenie egzemplarza jako demonstracyjnego (Rysunek 38). */
    public void markAsDemo() {
        this.role = VehicleRole.DEMO;
    }

    public VinNumber getVin() {
        return vin;
    }

    public SpecificationId getSpecification() {
        return specification;
    }

    public VehicleRole getRole() {
        return role;
    }

    public VehicleState getState() {
        return state;
    }

    /** Może zwrócić null, gdy pojazd nie jest przypisany do żadnego zamówienia (model rozłączny). */
    public OrderId getOrder() {
        return order;
    }
}
