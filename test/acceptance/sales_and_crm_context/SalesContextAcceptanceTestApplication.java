package acceptance.sales_and_crm_context;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import salon.sales.application.domain.model.offer.OfferFactory;
import salon.sales.application.domain.model.order.OrderFactory;

/**
 * Spring configuration anchor for the acceptance tests of the Sales Context
 * (full Sales slice: REST + JPA + the event bus, with WireMock for outbound HTTP and a mocked broker).
 *
 * IMPORTANT: the component scan is restricted to {@code salon.sales} ONLY. The other bounded contexts
 * (Billing, Financing, Logistics, Catalog) are wired in {@code salon.bootstrap.SalonWiringConfiguration}
 * and pull in collaborators that are irrelevant here; scanning the whole {@code salon} tree would boot
 * those beans too and cascade unrelated failures. We therefore load just the Sales beans and provide
 * the two framework-free domain factories ourselves.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "salon.sales")
@EntityScan(basePackages = "salon.sales.infrastructure.out.persistence")
@EnableJpaRepositories(basePackages = "salon.sales.infrastructure.out.persistence")
public class SalesContextAcceptanceTestApplication {

    @Bean
    public OfferFactory offerFactory() {
        return new OfferFactory();
    }

    @Bean
    public OrderFactory orderFactory() {
        return new OrderFactory();
    }
}
