# Value Object Audit & CQS Guidelines

- **Scope:** All classes modelled with the `<<ValueObject>>` stereotype across aggregates.
- **Rule:** Value Objects MUST be immutable. No public method should mutate internal state.
- **Audit checklist:**
  - Review each `<<ValueObject>>` and ensure methods (if any) return new instances instead of mutating `this`.
  - Remove or refactor any setter-like operations; prefer `withX(...)` that returns a new instance.
  - Ensure `equals` / `hashCode` semantics are based on the contained value fields only.
- **Special case — `applyDiscount(...)` in `Offer`:**
  - `DiscountLimit` is used strictly as a read-only predicate/validator.
  - Any change resulting from applying a discount must be performed by the `Offer` aggregate root by replacing value objects (e.g. substituting `Discount` and `Money` instances), not by mutating the `Discount` or `Money` objects.
- **Persistence note:**
  - Surrogate keys created by the database (e.g. auto-generated `line_id`) are an infrastructure concern and must not appear in domain model docs or be relied upon in domain logic.

Adopt these checks as part of PR reviews for domain layer changes.
