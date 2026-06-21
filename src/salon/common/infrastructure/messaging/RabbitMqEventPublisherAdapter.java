package salon.common.infrastructure.messaging;

import com.rabbitmq.client.Channel;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A REAL adapter publishing events to RabbitMQ (NOT a mock).
 *
 * Implements the EventPublisher outbound port, so the application layer calls publish(event)
 * without knowing anything about the broker. Here the event is:
 *   1) serializowane do JSON,
 *   2) sent to the topic exchange with routing key = the simple class name of the event.
 *
 * Subscribers (other contexts) bind their queues to the routing keys they are interested in.
 */
public class RabbitMqEventPublisherAdapter implements EventPublisher {

    private final RabbitMqConnection connection;
    private final EventSerializer serializer;

    public RabbitMqEventPublisherAdapter(RabbitMqConnection connection, EventSerializer serializer) {
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null.");
        }
        if (serializer == null) {
            throw new IllegalArgumentException("serializer must not be null.");
        }
        this.connection = connection;
        this.serializer = serializer;
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        String routingKey = event.getClass().getSimpleName();
        String json = this.serializer.toJson(event);
        byte[] body = json.getBytes(StandardCharsets.UTF_8);

        Channel channel = this.connection.channel();
        try {
            channel.basicPublish(RabbitMqConfig.EXCHANGE, routingKey, null, body);
            System.out.println("[RabbitMqPublisher] -> exchange=" + RabbitMqConfig.EXCHANGE
                    + " routingKey=" + routingKey + " body=" + json);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to publish event " + routingKey, e);
        }
    }
}
