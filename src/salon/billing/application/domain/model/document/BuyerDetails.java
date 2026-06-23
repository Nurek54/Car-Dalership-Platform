package salon.billing.application.domain.model.document;

public record BuyerDetails(String name, String nip) {

    public BuyerDetails {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank.");
        }
        if (nip == null || nip.isBlank()) {
            throw new IllegalArgumentException("nip must not be blank.");
        }
    }

    
    public boolean isCorporate() {
        String digits = this.nip.replaceAll("\\D", "");
        return digits.length() == 10;
    }
}
