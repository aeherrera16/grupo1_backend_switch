package com.banquito.core.enums;

import lombok.Getter;

@Getter
public enum CustomerStatusEnum {

    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO"),
    SUSPENDIDO("SUSPENDIDO");

    private final String value;

    CustomerStatusEnum(String value) {
        this.value = value;
    }
}
