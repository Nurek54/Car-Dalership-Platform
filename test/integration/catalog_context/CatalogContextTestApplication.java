package integration.catalog_context;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Spring configuration anchor for the tests in the integration.catalog_context package
 * (@SpringBootTest, @DataJpaTest, @WebMvcTest).
 *
 * The production composition root (salon.bootstrap.SalonApplication) lies outside the tree
 * test packages, so Spring Boot would not find it. The anchor reproduces the behavior
 * of @SpringBootApplication (TypeExcludeFilter lets the slices filter beans),
 * and @AutoConfigurationPackage points to the JPA entities and repositories in the salon.* tree.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@AutoConfigurationPackage(basePackages = "salon")
@ComponentScan(
        value = "salon",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
                @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class),
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = SpringBootApplication.class)
        }
)
public class CatalogContextTestApplication {
}
