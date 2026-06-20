package salon.logistics.infrastructure.in.web;

import salon.logistics.application.port.in.ReceiveVehicle;

/**
 * ADAPTER WEJŚCIOWY (sterujący) – rejestracja zjazdu pojazdu z lawety przez Pracownika Placu
 * (UC-INW-03). Realizuje port {@link ReceiveVehicle}; w realnym wdrożeniu metoda byłaby
 * mapowana na żądanie REST (np. @PostMapping("/yard/arrivals")). Nie zawiera logiki biznesowej.
 */
public class VehicleArrivalRestAdapter {

    private final ReceiveVehicle receiveVehicle;

    public VehicleArrivalRestAdapter(ReceiveVehicle receiveVehicle) {
        if (receiveVehicle == null) {
            throw new IllegalArgumentException("receiveVehicle must not be null.");
        }
        this.receiveVehicle = receiveVehicle;
    }

    /** Skan VIN przy przyjęciu pojazdu na plac. */
    public void onVehicleArrival(VehicleArrivalRequest request) {
        if (request == null || request.vin() == null || request.vin().isBlank()) {
            throw new IllegalArgumentException("vin must not be blank.");
        }
        this.receiveVehicle.receiveVehicle(request.vin());
    }

    /** Minimalne dane wejściowe adaptera (numer VIN zeskanowanego pojazdu). */
    public record VehicleArrivalRequest(String vin) {
    }
}
