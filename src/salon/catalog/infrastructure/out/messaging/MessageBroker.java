package salon.catalog.infrastructure.out.messaging;

public interface MessageBroker {

    
    void send(String routingKey, String payload);
}
