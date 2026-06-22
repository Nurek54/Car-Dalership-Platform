package salon.sales.infrastructure.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.common.model.OrderId;
import salon.sales.application.command.ScheduleHandoverCommand;
import salon.sales.application.service.SalesService;

import java.time.LocalDate;

/**
 * INBOUND ADAPTER (Figure 22: RestController) — exposes the order-lifecycle use cases over HTTP:
 * scheduling the handover (UC-CRM-04), confirming the handover (UC-CRM-05) and cancelling the
 * order (UC-CRM-03 / A1).
 */
@RestController
@RequestMapping("/api/sales/orders")
public class OrderRestApiAdapter {

    private final SalesService salesService;

    public OrderRestApiAdapter(SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping("/{id}/schedule-handover")
    public ResponseEntity<Void> scheduleHandover(@PathVariable("id") String orderId,
                                                 @Valid @RequestBody ScheduleHandoverRequest request) {
        this.salesService.scheduleHandover(new ScheduleHandoverCommand(orderId, request.handoverDate()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/handover")
    public ResponseEntity<Void> handover(@PathVariable("id") String orderId) {
        this.salesService.confirmHandover(new OrderId(orderId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable("id") String orderId,
                                       @RequestBody CancelOrderRequest request) {
        this.salesService.cancelOrder(new OrderId(orderId), request.reason());
        return ResponseEntity.ok().build();
    }

    /** Request body for scheduling a handover; the date is mandatory. */
    public record ScheduleHandoverRequest(@NotNull LocalDate handoverDate) {
    }

    /** Request body for cancelling an order. */
    public record CancelOrderRequest(String reason) {
    }
}
