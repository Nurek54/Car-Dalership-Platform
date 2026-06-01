package salon.shared.infrastructure.messaging;

/**
 * Stałe konfiguracyjne brokera. Jeden topic-exchange dla całego salonu;
 * routing key = prosta nazwa klasy zdarzenia (np. "DepositRegisteredEvent").
 *
 * Połączenie (host/port/login) czytamy ze zmiennych środowiskowych z sensownym defaultem:
 *  - z IDE / `mvn exec:java` (na hoście) brak ENV -> "localhost" (jak dotychczas),
 *  - w kontenerze ustawiamy RABBITMQ_HOST=rabbitmq (nazwa serwisu w docker-compose).
 */
public final class RabbitMqConfig {

    private RabbitMqConfig() {
    }

    public static final String EXCHANGE = "salon.domain-events";

    // Kolejki per kontekst-subskrybent.
    public static final String SALES_QUEUE = "sales.inbox";
    public static final String BILLING_QUEUE = "billing.inbox";

    // Połączenie — ENV z defaultem (default = lokalny broker rabbitmq:3-management).
    public static final String HOST = envOrDefault("RABBITMQ_HOST", "localhost");
    public static final int PORT = Integer.parseInt(envOrDefault("RABBITMQ_PORT", "5672"));
    public static final String USERNAME = envOrDefault("RABBITMQ_USER", "guest");
    public static final String PASSWORD = envOrDefault("RABBITMQ_PASS", "guest");

    private static String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}