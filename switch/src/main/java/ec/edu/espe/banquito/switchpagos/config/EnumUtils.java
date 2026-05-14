package ec.edu.espe.banquito.switchpagos.config;

import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.enums.ChannelEnum;
import ec.edu.espe.banquito.switchpagos.enums.ServiceTypeEnum;

/**
 * Utility class for enum conversions and common operations.
 */
public class EnumUtils {

    public static boolean isValidationSuccess(String value) {
        return value != null && "SUCCESS".equalsIgnoreCase(value.trim());
    }

    /**
     * Safely converts a string to ChannelEnum, returns null if invalid.
     */
    public static ChannelEnum safeChannelEnumFromString(String value) {
        try {
            if (value == null) return null;
            return ChannelEnum.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                return ChannelEnum.fromDisplayName(value);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    /**
     * Safely converts a string to ServiceTypeEnum, returns null if invalid.
     */
    public static ServiceTypeEnum safeServiceTypeEnumFromString(String value) {
        try {
            if (value == null) return null;
            return ServiceTypeEnum.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                return ServiceTypeEnum.fromDisplayName(value);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    /**
     * Safely converts a string to BatchStatusEnum, returns null if invalid.
     */
    public static BatchStatusEnum safeBatchStatusEnumFromString(String value) {
        try {
            if (value == null) return null;
            return BatchStatusEnum.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                return BatchStatusEnum.fromDisplayName(value);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }
}
