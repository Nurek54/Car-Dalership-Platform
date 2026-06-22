package salon.common.infrastructure.messaging;

public final class RabbitMqConfig {

    private RabbitMqConfig() {
    }

    public static final String EXCHANGE = "salon.domain-events";

    public static final String SALES_QUEUE = "sales.inbox";
    public static final String BILLING_QUEUE = "billing.inbox";

    public static final String HOST = envOrDefault("RABBITMQ_HOST", "localhost");
    public static final int PORT = Integer.parseInt(envOrDefault("RABBITMQ_PORT", "5672"));
    public static final String USERNAME = envOrDefault("RABBITMQ_USER", "guest");
    public static final String PASSWORD = envOrDefault("RABBITMQ_PASS", "guest");

    private static String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
