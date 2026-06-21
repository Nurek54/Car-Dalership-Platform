package salon.logistics.infrastructure.in.web;

import salon.logistics.application.port.in.ReceiveVehicle;

/**
 * INBOUND ADAPTER (driving) – registration of a vehicle's unloading from the transporter by the Yard Worker
 * (UC-INW-03). Implements the {@link ReceiveVehicle} port; in a real deployment the method would be
 * mapped to a REST request (e.g. @PostMapping("/yard/arrivals")). It contains no business logic.
 */
public class VehicleArrivalRestAdapter {

    private final ReceiveVehicle receiveVehicle;

    public VehicleArrivalRestAdapter(ReceiveVehicle receiveVehicle) {
        if (receiveVehicle == null) {
            throw new IllegalArgumentException("receiveVehicle must not be null.");
        }
        this.receiveVehicle = receiveVehicle;
    }

    /** VIN scan when receiving the vehicle into the yard. */
    public void onVehicleArrival(VehicleArrivalRequest request) {
        if (request == null || request.vin() == null || request.vin().isBlank()) {
            throw new IllegalArgumentException("vin must not be blank.");
        }
        this.receiveVehicle.receiveVehicle(request.vin());
    }

    /** Minimal input data of the adapter (the VIN of the scanned vehicle). */
    public record VehicleArrivalRequest(String vin) {
    }
}
