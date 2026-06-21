package salon.common.infrastructure.messaging;

import java.util.Map;

/**
 * Contract for handling a single, parsed event received from the queue.
 * A concrete context registers a handler for the event type it is interested in.
 */
public interface RabbitMqMessageHandler {
    void handle(Map<String, String> event);
}
