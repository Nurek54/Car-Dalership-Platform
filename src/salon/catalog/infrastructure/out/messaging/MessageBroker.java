package salon.catalog.infrastructure.out.messaging;

/**
 * Technical boundary of the message broker ("RabbitMq" from the diagram) – a thin contract
 * to the message-passing middleware (push). The concrete client (Spring AMQP /
 * RabbitMQ) lies outside the context and is provided by the deployment infrastructure.
 */
public interface MessageBroker {

    /**
     * @param routingKey the destination key/topic (e.g. the event name)
     * @param payload    the serialized message body
     */
    void send(String routingKey, String payload);
}
