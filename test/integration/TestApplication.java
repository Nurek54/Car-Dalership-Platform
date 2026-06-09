package integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Kotwica konfiguracji dla testów plasterkowych (@DataJpaTest, @WebMvcTest).
 *
 * Produkcyjny korzeń kompozycji (salon.bootstrap.SalonApplication) leży poza drzewem
 * pakietów testów integracyjnych (integration.*), więc Spring Boot nie odnajdzie go,
 * idąc w górę pakietu testu. Ta klasa pełni rolę kotwicy dla testów plasterkowych.
 *
 * @ComponentScan("salon") pozwala @WebMvcTest wykryć kontrolery i @ControllerAdvice
 * w drzewie salon.*. Plasterki (@WebMvcTest, @DataJpaTest) stosują własne filtry typów,
 * więc nie ładują całego kontekstu — @WebMvcTest bierze tylko kontrolery i web-beany,
 * @DataJpaTest tylko encje i repozytoria.
 *
 * excludeFilters wyklucza SalonApplication (@SpringBootApplication) ze skanowania,
 * żeby uniknąć podwójnej rejestracji @EnableJpaRepositories w testach plasterkowych.
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
