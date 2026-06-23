package salon.common.infrastructure.messaging;

import salon.common.event.DomainEvent;

import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.Map;

public class RecordEventSerializer implements EventSerializer {

    @Override
    public String toJson(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("type", event.getClass().getSimpleName());

        RecordComponent[] components = event.getClass().getRecordComponents();
        if (components == null) {
            throw new IllegalArgumentException(
                    "Event " + event.getClass().getName() + " must be a record.");
        }
        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            String name = component.getName();
            Object value = readComponent(event, component);
            if (value == null) {
                fields.put(name, null);
            } else {
                fields.put(name, value.toString());
            }
        }
        return EventJson.write(fields);
    }

    private Object readComponent(DomainEvent event, RecordComponent component) {
        try {
            return component.getAccessor().invoke(event);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Cannot read field " + component.getName() + " of " + event.getClass().getName(), e);
        }
    }
}
