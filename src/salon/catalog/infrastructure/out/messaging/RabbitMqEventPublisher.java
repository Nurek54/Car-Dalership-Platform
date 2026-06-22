package salon.catalog.infrastructure.out.messaging;

import salon.catalog.application.port.out.EventPublisher;
import salon.catalog.application.domain.model.event.CatalogUpdateFailed;
import salon.catalog.application.domain.model.event.CatalogUpdated;
import salon.catalog.application.domain.model.event.DomainEvent;
import salon.catalog.application.domain.model.event.SpecificationCompleted;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqEventPublisher implements EventPublisher {

    private final MessageBroker broker;

    public RabbitMqEventPublisher(MessageBroker broker) {
        this.broker = broker;
    }

    @Override
    public void publish(DomainEvent event) {
        String routingKey = "catalog." + event.eventName();
        broker.send(routingKey, serialize(event));
    }

    private String serialize(DomainEvent event) {
        StringBuilder sb = new StringBuilder("{")
                .append("\"event\":\"").append(event.eventName()).append("\",")
                .append("\"occurredOn\":\"").append(event.occurredOn()).append("\"");

        if (event instanceof SpecificationCompleted e) {
            sb.append(",\"specificationId\":\"").append(e.specificationId()).append("\"")
              .append(",\"catalogId\":\"").append(e.catalogId()).append("\"")
              .append(",\"totalPrice\":\"").append(e.totalPrice()).append("\"")
              .append(",\"options\":\"").append(e.optionsPicked()).append("\"");
        } else if (event instanceof CatalogUpdated e) {
            sb.append(",\"catalogId\":\"").append(e.catalogId()).append("\"")
              .append(",\"modelYear\":\"").append(e.modelYear()).append("\"")
              .append(",\"version\":").append(e.version());
        } else if (event instanceof CatalogUpdateFailed e) {
            sb.append(",\"reason\":\"").append(e.reason()).append("\"");
        }

        return sb.append("}").toString();
    }
}
