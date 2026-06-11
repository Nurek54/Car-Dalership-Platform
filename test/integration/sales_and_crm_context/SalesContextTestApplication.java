package integration.sales_and_crm_context;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Kotwica konfiguracji Springa dla testów pakietu integration.sales_and_crm_context
 * (@SpringBootTest, @DataJpaTest, @WebMvcTest).
 *
 * Produkcyjny korzeń kompozycji (salon.bootstrap.SalonApplication) leży poza drzewem
 * pakietów testów, więc Spring Boot by go nie odnalazł. Kotwica odtwarza zachowanie
 * @SpringBootApplication (TypeExcludeFilter pozwala plasterkom filtrować beany),
 * a @AutoConfigurationPackage wskazuje encje i repozytoria JPA w drzewie salon.*.
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
public class SalesContextTestApplication {
}
