package ec.edu.espe.banquito.switchpagos.enums;

public enum ChargeStatusEnum {
    PENDING("Pendiente"),
    CHARGED("Cargado"),
    REJECTED("Rechazado");

    private final String displayName;

    ChargeStatusEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ChargeStatusEnum fromDisplayName(String displayName) {
        for (ChargeStatusEnum status : values()) {
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
