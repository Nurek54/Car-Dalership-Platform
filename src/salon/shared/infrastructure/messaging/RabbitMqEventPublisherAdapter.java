package salon.shared.infrastructure.messaging;

import com.rabbitmq.client.Channel;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * PRAWDZIWY adapter publikujący zdarzenia do RabbitMQ (NIE mock).
 *
 * Implementuje port wyjściowy EventPublisherPort, więc warstwa aplikacji woła publish(event)
 * nie wiedząc nic o brokerze. Tutaj zdarzenie jest:
 *   1) serializowane do JSON,
 *   2) wysyłane na topic-exchange z routing key = prosta nazwa klasy zdarzenia.
 *
 * Subskrybenci (inne konteksty) wiążą swoje kolejki do interesujących ich routing keys.
 */
public class RabbitMqEventPublisherAdapter implements EventPublisherPort {

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
