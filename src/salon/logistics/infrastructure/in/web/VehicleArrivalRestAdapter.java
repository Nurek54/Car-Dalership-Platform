package salon.logistics.infrastructure.in.web;

import salon.logistics.application.port.in.ReceiveVehicle;

public class VehicleArrivalRestAdapter {

    private final ReceiveVehicle receiveVehicle;

    public VehicleArrivalRestAdapter(ReceiveVehicle receiveVehicle) {
        if (receiveVehicle == null) {
            throw new IllegalArgumentException("receiveVehicle must not be null.");
        }
        this.receiveVehicle = receiveVehicle;
    }

    
    public void onVehicleArrival(VehicleArrivalRequest request) {
        if (request == null || request.vin() == null || request.vin().isBlank()) {
            throw new IllegalArgumentException("vin must not be blank.");
        }
        this.receiveVehicle.receiveVehicle(request.vin());
    }

    
    public record VehicleArrivalRequest(String vin) {
    }
}
