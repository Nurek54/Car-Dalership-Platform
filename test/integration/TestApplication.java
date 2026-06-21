package integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Configuration anchor for the slice tests (@DataJpaTest, @WebMvcTest).
 *
 * The production composition root (salon.bootstrap.SalonApplication) lies outside the tree
 * of the integration test packages (integration.*), so Spring Boot will not find it
 * by going up the test package. This class serves as the anchor for the slice tests.
 *
 * @ComponentScan("salon") lets @WebMvcTest detect the controllers and @ControllerAdvice
 * in the salon.* tree. The slices (@WebMvcTest, @DataJpaTest) apply their own type filters,
 * so they do not load the whole context — @WebMvcTest takes only the controllers and web beans,
 * @DataJpaTest only the entities and repositories.
 *
 * excludeFilters wyklucza SalonApplication (@SpringBootApplication) ze skanowania,
 * to avoid double registration of @EnableJpaRepositories in the slice tests.
 */
@SpringBootConfiguration
@ComponentScan(
    value = "salon",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = SpringBootApplication.class
    )
)
public class TestApplication {
}
