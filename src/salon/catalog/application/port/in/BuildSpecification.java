package salon.catalog.application.port.in;

import salon.catalog.application.command.AddOptionCommand;
import salon.catalog.application.command.FinalizeSpecificationCommand;
import salon.catalog.application.command.InitiateConfiguratorSessionCommand;
import salon.catalog.application.command.RemoveOptionCommand;
import salon.catalog.application.dto.SpecificationView;

public interface BuildSpecification {

    SpecificationView initiate(InitiateConfiguratorSessionCommand command);

    SpecificationView addOption(AddOptionCommand command);

    SpecificationView removeOption(RemoveOptionCommand command);

    SpecificationView finalizeSpecification(FinalizeSpecificationCommand command);
}
