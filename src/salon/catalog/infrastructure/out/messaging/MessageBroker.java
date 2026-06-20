package salon.catalog.infrastructure.out.messaging;

/**
 * Techniczna granica brokera komunikatów („RabbitMq” z diagramu) – cienki kontrakt
 * do middleware przekazywania komunikatów (push). Konkretny klient (Spring AMQP /
 * RabbitMQ) leży poza kontekstem i jest dostarczany przez infrastrukturę wdrożeniową.
 */
public interface MessageBroker {

    /**
     * @param routingKey klucz/temat docelowy (np. nazwa zdarzenia)
     * @param payload    zserializowana treść komunikatu
     */
    void send(String routingKey, String payload);
}
