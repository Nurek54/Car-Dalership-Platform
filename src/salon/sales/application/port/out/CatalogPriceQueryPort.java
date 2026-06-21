package salon.sales.application.port.out;

import salon.common.model.Money;

/**
 * Outbound port (driven) of the Sales Context — a synchronous query for the catalog
 * price of the specification (HTTP integration with the Catalog module).
 *
 * The Dependency Inversion Principle (the D in SOLID): it is the SALES CONTEXT that defines what
 * it needs from the Catalog (its own port), and an adapter in the infrastructure layer implements
 * this contract. Sales no longer depends on the Catalog repository port.
 *
 * Note: in the event-driven mode (event-carried state transfer) the pricing arrives
 * asynchronously through {@link SpecificationPriceReadModelPort} from the
 * SpecificationCompleted event; this port remains for synchronous integration when required.
 */
public interface CatalogPriceQueryPort {

    Money specificationPrice(String specificationId);
}
