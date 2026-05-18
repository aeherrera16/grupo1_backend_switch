package ec.edu.espe.banquito.switchpagos.enums;

public enum ServiceTypeEnum {
    NOM("Nómina"),
    PRV("Proveedores");

    private final String displayName;

    ServiceTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ServiceTypeEnum fromDisplayName(String displayName) {
        for (ServiceTypeEnum service : values()) {
            if (service.displayName.equals(displayName)) {
                return service;
            }
        }
        throw new IllegalArgumentException("Unknown display name: " + displayName);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
