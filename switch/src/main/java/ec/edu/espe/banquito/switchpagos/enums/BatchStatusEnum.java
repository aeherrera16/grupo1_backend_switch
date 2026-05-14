package ec.edu.espe.banquito.switchpagos.enums;

public enum BatchStatusEnum {
    RECEIVED("Recibido"),
    VALIDATED("Validado"),
    PROCESSING("En Proceso"),
    PROCESSED("Procesado"),
    REJECTED("Rechazado"),
    ENCOLADO("Encolado");

    private final String displayName;

    BatchStatusEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BatchStatusEnum fromDisplayName(String displayName) {
        for (BatchStatusEnum status : values()) {
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
