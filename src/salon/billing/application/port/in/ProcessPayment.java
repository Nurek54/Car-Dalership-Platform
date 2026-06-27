package salon.billing.application.port.in;

import salon.billing.application.command.ProcessPaymentCommand;
import salon.common.model.Money;
import salon.common.model.OrderId;

public interface ProcessPayment {

    
    void initializeSettlement(OrderId orderId, Money totalAmount);

    
    void processPayment(ProcessPaymentCommand command);

    
    void sendPaymentReminders();
}
