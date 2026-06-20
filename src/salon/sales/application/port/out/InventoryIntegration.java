package salon.sales.application.port.out;

/**
 * Port wyjściowy (driven) do Kontekstu Inwentarza i Logistyki —
 * węzeł "InventoryIntegration" w docs/Architecture/SalesArchitecture.md
 * (PDF rozdz. 3.3.3: "rezerwacje, zwalnianie blokad na placu").
 */
public interface InventoryIntegration {

    /** UC-CRM-03/UC-INW-01..02: alokacja pojazdu z placu lub slotu produkcyjnego dla zamówienia. */
    void allocateVehicleOrProductionSlot(String orderId);

    /** UC-CRM-05, krok 3: komenda ReleaseVehicle — zdjęcie fizycznego auta ze stanu (UC-INW-06). */
    void releasePhysicalVehicle(String vehicleId);
}
