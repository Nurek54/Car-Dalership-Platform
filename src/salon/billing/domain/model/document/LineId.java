package salon.billing.domain.model.document;

// Tożsamość pozycji dokumentu (lokalna w obrębie agregatu).
public record LineId(Long value) {

    public LineId {
        if (value == null) {
            throw new IllegalArgumentException("LineId must not be null.");
        }
    }
}
