package salon.common.infrastructure.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Cienka obudowa na połączenie z RabbitMQ (klient com.rabbitmq:amqp-client).
 * Tworzy połączenie + kanał i deklaruje nasz exchange. Jeden obiekt na proces.
 */
public class RabbitMqConnection implements AutoCloseable {

    private final Connection connection;
    private final Channel channel;

    public RabbitMqConnection() {
        this(RabbitMqConfig.HOST, RabbitMqConfig.PORT,
                RabbitMqConfig.USERNAME, RabbitMqConfig.PASSWORD);
    }

    public RabbitMqConnection(String host, int port, String username, String password) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        try {
            this.connection = factory.newConnection();
            this.channel = this.connection.createChannel();
            // Topic exchange — pozwala bindować po wzorcu routing key.
            this.channel.exchangeDeclare(RabbitMqConfig.EXCHANGE, "topic", true);
        } catch (IOException | TimeoutException e) {
            throw new IllegalStateException("Cannot connect to RabbitMQ at "
                    + host + ":" + port + " — is the broker running?", e);
        }
    }

    public Channel channel() {
        return this.channel;
    }

    @Override
    public void close() {
        try {
            if (this.channel != null && this.channel.isOpen()) {
                this.channel.close();
            }
            if (this.connection != null && this.connection.isOpen()) {
                this.connection.close();
            }
        } catch (IOException | TimeoutException e) {
            // Zamykanie best-effort — logujemy i idziemy dalej.
            System.out.println("[RabbitMqConnection] Error while closing: " + e.getMessage());
        }
    }
}
