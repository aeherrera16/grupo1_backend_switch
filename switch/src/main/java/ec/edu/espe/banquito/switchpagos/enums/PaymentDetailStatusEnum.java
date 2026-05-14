package ec.edu.espe.banquito.switchpagos.enums;

public enum PaymentDetailStatusEnum {
    PENDING("Pendiente"),
    SUCCESS("Exitoso"),
    REJECTED("Rechazado");

    private final String displayName;

    PaymentDetailStatusEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentDetailStatusEnum fromDisplayName(String displayName) {
        for (PaymentDetailStatusEnum status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown display name: " + displayName);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
