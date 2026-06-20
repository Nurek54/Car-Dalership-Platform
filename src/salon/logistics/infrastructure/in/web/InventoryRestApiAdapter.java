package salon.logistics.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.logistics.application.port.in.OrderFactoryVehicleUseCase;
import salon.logistics.application.port.in.PrepareForHandover;
import salon.logistics.application.port.in.ReceiveVehicle;

/**
 * Adapter sterujacy (driving) — REST API dla pracownikow placu i logistykow (PDF rozdz. 3.5.3,
 * "Adaptery HTTP ... dla pracownikow placu"). Obsluguje reczny obrot pojazdami:
 * UC-INW-02 (zlecenie produkcji), UC-INW-03 (przyjecie na stan), UC-INW-05 (przygotowanie do wydania).
 * Cienki: parsuje zadania i deleguje do portow wejsciowych.
 */
@RestController
@RequestMapping("/api/logistics")
public class InventoryRestApiAdapter {

    private final OrderFactoryVehicleUseCase orderFactoryVehicle;
    private final ReceiveVehicle receiveVehicle;
    private final PrepareForHandover prepareForHandover;

    public InventoryRestApiAdapter(OrderFactoryVehicleUseCase orderFactoryVehicle,
                                   ReceiveVehicle receiveVehicle,
                                   PrepareForHandover prepareForHandover) {
        if (orderFactoryVehicle == null || receiveVehicle == null || prepareForHandover == null) {
            throw new IllegalArgumentException("use cases must not be null.");
        }
        this.orderFactoryVehicle = orderFactoryVehicle;
        this.receiveVehicle = receiveVehicle;
        this.prepareForHandover = prepareForHandover;
    }

    /** UC-INW-02: zlecenie produkcji pojazdu w fabryce. */
    @PostMapping("/factory-orders")
    public ResponseEntity<Void> orderFromFactory(@RequestBody OrderIdRequest request) {
        orderFactoryVehicle.orderVehicleFromFactory(request.orderId());
        return ResponseEntity.ok().build();
    }

    /** UC-INW-03: przyjecie pojazdu na stan magazynowy. */
    @PostMapping("/vehicles/{vin}/receive")
    public ResponseEntity<Void> receive(@PathVariable String vin) {
        receiveVehicle.receiveVehicle(vin);
        return ResponseEntity.ok().build();
    }

    /** UC-INW-05: przygotowanie pojazdu do wydania po rozliczeniu. */
    @PostMapping("/handovers")
    public ResponseEntity<Void> prepareHandover(@RequestBody OrderIdRequest request) {
        prepareForHandover.prepareVehicleForHandover(request.orderId());
        return ResponseEntity.ok().build();
    }

    public record OrderIdRequest(String orderId) {}
}
