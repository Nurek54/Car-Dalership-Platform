package salon.catalog.application.port.in;

import salon.catalog.application.command.AddOptionCommand;
import salon.catalog.application.command.FinalizeSpecificationCommand;
import salon.catalog.application.command.InitiateConfiguratorSessionCommand;
import salon.catalog.application.command.RemoveOptionCommand;
import salon.catalog.application.dto.SpecificationView;

/**
 * INBOUND PORT (contract) – "BuildSpecification" from the ports-and-adapters diagram.
 *
 * Exposes the services provided by the context for the actors initiating UC-KON-01
 * (the InitiateConfiguratorSession event from CRM and the user interface).
 * Belongs to the application layer; implemented by the application service.
 *
 * In line with the Interface Segregation Principle (the I in SOLID) the port has one purpose:
 * preparing and finalizing the vehicle specification.
 */
public interface BuildSpecification {

    /** Step 1: opening a configurator session for the active catalog of a given model year. */
    SpecificationView initiate(InitiateConfiguratorSessionCommand command);

    /** Steps 2–4: adding an option with on-the-fly verification of exclusion rules. */
    SpecificationView addOption(AddOptionCommand command);

    /** Changing the selection: removing a previously added option. */
    SpecificationView removeOption(RemoveOptionCommand command);

    /** Steps 5–7: finalization and emission of the SpecificationCompleted event. */
    SpecificationView finalizeSpecification(FinalizeSpecificationCommand command);
}
