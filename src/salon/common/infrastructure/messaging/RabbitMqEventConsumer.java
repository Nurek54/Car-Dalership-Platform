package salon.common.infrastructure.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A REAL RabbitMQ event consumer (NOT a mock).
 *
 * Declares the context's queue, binds it to the exchange for each registered event type
 * and listens. After receiving a message: it parses the JSON, reads the "type" field and calls the handler.
 *
 * Deduplication (idempotency, section 3.4.2) is deliberately OMITTED here — it belongs to
 * the subscriber (the specific listener of a given context), exactly as in SettlementEventListener.
 */
public class RabbitMqEventConsumer {

    private final RabbitMqConnection connection;
    private final String queueName;
    private final Map<String, RabbitMqMessageHandler> handlers = new HashMap<>();

    public RabbitMqEventConsumer(RabbitMqConnection connection, String queueName) {
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null.");
        }
        if (queueName == null || queueName.isBlank()) {
            throw new IllegalArgumentException("queueName must not be blank.");
        }
        this.connection = connection;
        this.queueName = queueName;
    }

    // We register interest in a given event type (e.g. "PaymentRegisteredEvent").
    public void register(String eventType, RabbitMqMessageHandler handler) {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be blank.");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null.");
        }
        this.handlers.put(eventType, handler);
    }

    public void start() {
        Channel channel = this.connection.channel();
        try {
            channel.queueDeclare(this.queueName, true, false, false, null);
            // We bind the queue to the exchange for each type we are interested in.
            for (Map.Entry<String, RabbitMqMessageHandler> entry : this.handlers.entrySet()) {
                channel.queueBind(this.queueName, RabbitMqConfig.EXCHANGE, entry.getKey());
            }

            DeliverCallback onDeliver = (consumerTag, delivery) -> {
                String json = new String(delivery.getBody(), StandardCharsets.UTF_8);
                Map<String, String> event = EventJson.read(json);
                String type = event.get("type");
                System.out.println("[RabbitMqConsumer:" + this.queueName + "] <- " + json);
                RabbitMqMessageHandler handler = this.handlers.get(type);
                if (handler != null) {
                    handler.handle(event);
                }
            };

            channel.basicConsume(this.queueName, true, onDeliver, consumerTag -> {
            });
            System.out.println("[RabbitMqConsumer:" + this.queueName + "] listening for "
                    + this.handlers.keySet());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start consumer on queue " + this.queueName, e);
        }
    }
}
