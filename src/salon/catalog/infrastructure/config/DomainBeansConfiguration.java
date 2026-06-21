package salon.catalog.infrastructure.config;

import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;
import salon.catalog.application.domain.service.RuleValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configuration layer (infrastructure) – a neutral component assembling the system
 * from parts via dependency injection.
 *
 * The domain model (factories, domain service) is FREE of framework dependencies
 * (no Spring annotations), so we create its instances here and expose them as
 * beans. The class belongs to the infrastructure layer (salon.catalog.infrastructure.config),
 * which – per the hexagonal architecture – may reference the application layer
 * and the domain. Adapters and application services are discovered via component scanning.
 */
@Configuration
public class DomainBeansConfiguration {

    @Bean
    public ProductCatalogFactory productCatalogFactory() {
        return new ProductCatalogFactory();
    }

    @Bean
    public VehicleSpecificationFactory vehicleSpecificationFactory() {
        return new VehicleSpecificationFactory();
    }

    @Bean
    public RuleValidationService ruleValidationService() {
        return new RuleValidationService();
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
