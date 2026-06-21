package salon.common.infrastructure.messaging;

/**
 * Broker configuration constants. A single topic exchange for the whole dealership;
 * routing key = the simple class name of the event (e.g. "PaymentRegisteredEvent").
 *
 * We read the connection (host/port/login) from environment variables with a sensible default:
 *  - from the IDE / `mvn exec:java` (on the host) no ENV -> "localhost" (as before),
 *  - w kontenerze ustawiamy RABBITMQ_HOST=rabbitmq (nazwa serwisu w docker-compose).
 */
public final class RabbitMqConfig {

    private RabbitMqConfig() {
    }

    public static final String EXCHANGE = "salon.domain-events";

    // Queues per subscriber context.
    public static final String SALES_QUEUE = "sales.inbox";
    public static final String BILLING_QUEUE = "billing.inbox";

    // Connection — ENV with a default (default = local broker rabbitmq:3-management).
    public static final String HOST = envOrDefault("RABBITMQ_HOST", "localhost");
    public static final int PORT = Integer.parseInt(envOrDefault("RABBITMQ_PORT", "5672"));
    public static final String USERNAME = envOrDefault("RABBITMQ_USER", "guest");
    public static final String PASSWORD = envOrDefault("RABBITMQ_PASS", "guest");

    private static String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}