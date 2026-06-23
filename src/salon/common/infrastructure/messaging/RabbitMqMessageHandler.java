package salon.common.infrastructure.messaging;

import java.util.Map;

public interface RabbitMqMessageHandler {
    void handle(Map<String, String> event);
}
