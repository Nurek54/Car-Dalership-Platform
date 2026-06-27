package salon.catalog.infrastructure.config;

import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;
import salon.catalog.application.domain.service.RuleValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

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
