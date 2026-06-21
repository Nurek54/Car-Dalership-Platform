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
     * The composition root (salon.bootstrap.* — runnable main: SalonDemo/MessagingDemo) deliberately
     * wires together all layers (that is the "Composition Root"/wiring task). It is not subject to the
     * hexagonal-layer rules, so we exclude it from the analysis — per the architecture documentation.
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

            // Defining the direction of dependencies
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Infrastructure")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Application")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Domain")

            // A documented exception (see docs/Architecture/CatalogArchitecture.md): a stateless
            // DOMAIN SERVICE (..domain.service..) itself fetches the aggregate from the repository (the
            // outbound in the application layer), so that rule validation (Fail-fast) runs in the domain.
            .ignoreDependency(resideInAPackage("..domain.service.."), resideInAPackage("..application.."));

    @ArchTest
    static final ArchRule domainShouldNotDependOnExternalFrameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .orShould().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
            .because("In Domain Layer there shouldn't be framework code!");

    @ArchTest
    static final ArchRule allContextsShouldBeIndependent = slices()
            .matching("salon.(*).application.domain..")
            .should().notDependOnEachOther()
            .because("Contexts should be isolated. We using Domain Events!");

    @ArchTest       // ports should be interfaces
    static final ArchRule portsShouldBeInterfaces = classes()
            .that().resideInAPackage("..application.port..")
            .should().beInterfaces()
            .because("Ports in Hexagonal Architecture must be interfaces!");

    @ArchTest       // Events must be Records
    static final ArchRule domainEventsShouldBeRecords = classes()
            .that().resideInAPackage("..domain.event..")
            .and().areTopLevelClasses()
            .should().beRecords()
            .because("Domain events are immutable!");
}
