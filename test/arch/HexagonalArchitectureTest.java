package arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "salon",
        importOptions = {ImportOption.DoNotIncludeTests.class, HexagonalArchitectureTest.DoNotIncludeBootstrap.class})
class HexagonalArchitectureTest {

    /**
     * Korzeń kompozycji (salon.bootstrap.* — uruchamialne main: SalonDemo/MessagingDemo) celowo
     * spina ze sobą wszystkie warstwy (to zadanie "Composition Root"/wiring). Nie podlega regułom
     * warstw heksagonalnych, więc wyłączamy go z analizy — zgodnie z dokumentacją architektury.
     */
    static final class DoNotIncludeBootstrap implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return !location.contains("salon/bootstrap/") && !location.contains("salon\\bootstrap\\");
        }
    }

    @ArchTest
    static final ArchRule strictlyEnforceHexagonalArchitecture = layeredArchitecture()
            .consideringAllDependencies()
            // Sprawdzi on automatycznie: salon.billing.domain, salon.sales.domain itd.
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")

            // Definiowanie kierunku zależności
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Infrastructure")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Application")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Domain")

            // Udokumentowany wyjątek (patrz docs/Architecture/CatalogArchitecture.md): bezstanowy
            // SERWIS DZIEDZINOWY (..domain.service..) sam pobiera agregat z repozytorium (port
            // wyjściowy w warstwie aplikacji), aby walidacja reguł (Fail-fast) działała w domenie.
            .ignoreDependency(resideInAPackage("..domain.service.."), resideInAPackage("..application.."));

    @ArchTest
    static final ArchRule domainShouldNotDependOnExternalFrameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .orShould().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
            .because("In Domain Layer there shouldn't be framework code!");

    @ArchTest
    static final ArchRule allContextsShouldBeIndependent = slices()
            .matching("salon.(*).domain..")
            .should().notDependOnEachOther()
            .because("Contexts should be isolated. We using Domain Events!");

    @ArchTest       // porty powinny być interfejsami
    static final ArchRule portsShouldBeInterfaces = classes()
            .that().resideInAPackage("..application.port..")
            .should().beInterfaces()
            .because("Ports in Hexagonal Architecture must be interfaces!");

    @ArchTest       // Eventy muszą być Rekordami
    static final ArchRule domainEventsShouldBeRecords = classes()
            .that().resideInAPackage("..domain.event..")
            .and().areTopLevelClasses()
            .should().beRecords()
            .because("Domain events are immutable!");
}
