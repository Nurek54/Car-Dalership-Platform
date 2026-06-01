package salon.shared.infrastructure.messaging;

import salon.shared.event.DomainEvent;

// Kontrakt serializacji zdarzenia do tekstu (JSON). Adapter publikujący zależy tylko od tego.
public interface EventSerializer {
    String toJson(DomainEvent event);
}
