package ec.edu.espe.banquito.switchpagos.enums;

/**
 * Channel types for file ingestion with full display names.
 */
public enum ChannelEnum {
    PORTAL("Portal Web"),
    SFTP("SFTP Seguro");

    private final String displayName;

    ChannelEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ChannelEnum fromDisplayName(String displayName) {
        for (ChannelEnum channel : values()) {
            if (channel.displayName.equals(displayName)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown display name: " + displayName);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
