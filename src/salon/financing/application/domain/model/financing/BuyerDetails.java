package salon.financing.application.domain.model.financing;

public record BuyerDetails(String name, String nip) {

    public BuyerDetails {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Buyer name is required.");
        }
    }

    public boolean isCorporate() {
        return this.nip != null && !this.nip.isBlank();
    }
}