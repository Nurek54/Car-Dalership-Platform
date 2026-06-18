package salon.common.infrastructure.messaging;

import java.util.Map;

/**
 * Kontrakt obsługi pojedynczego, sparsowanego zdarzenia przyjętego z kolejki.
 * Konkretny kontekst rejestruje handler dla typu zdarzenia, którym jest zainteresowany.
 */
public interface RabbitMqMessageHandler {
    void handle(Map<String, String> event);
}
