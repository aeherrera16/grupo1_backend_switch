package ec.edu.espe.banquito.switchpagos.enums;

public enum FileStatusEnum {
    UPLOADED("CARGADO"),
    VALIDATED("VALIDADO"),
    PROCESSED("PROCESADO"),
    PROCESSING("EN PROCESO"),
    PARTIAL_PROCESSED("PROCESADO PARCIAL"),
    REJECTED("RECHAZADO");

    private final String displayName;

    FileStatusEnum(String displayName) {
        this.displayName = displayName;
    }

}
