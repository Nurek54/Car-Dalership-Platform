package salon.shared.infrastructure.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * PRAWDZIWY konsument zdarzeń z RabbitMQ (NIE mock).
 *
 * Deklaruje kolejkę kontekstu, wiąże ją do exchange dla każdego zarejestrowanego typu zdarzenia
 * i nasłuchuje. Po odebraniu wiadomości: parsuje JSON, odczytuje pole "type" i woła handler.
 *
 * Deduplikacja (idempotencyjność, sekcja 3.4.2) jest tutaj świadomie POMINIĘTA — należy do
 * subskrybenta (konkretnego listenera danego kontekstu), dokładnie jak w SettlementEventListener.
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

    // Rejestrujemy zainteresowanie danym typem zdarzenia (np. "DepositRegisteredEvent").
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
            // Wiążemy kolejkę do exchange dla każdego typu, który nas interesuje.
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
