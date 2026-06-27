package salon.sales.api;

public record CustomerSnapshotDto(String fullName, String nip) {

    public CustomerSnapshotDto {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName must not be null or blank.");
        }
        if (nip == null || nip.isBlank()) {
            throw new IllegalArgumentException("nip must not be null or blank.");
        }
    }
}
