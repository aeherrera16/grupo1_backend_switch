package com.banquito.core.enums;

import lombok.Getter;

@Getter
public enum CustomerSubtypeStatusEnum {

    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO");

    private final String value;

    CustomerSubtypeStatusEnum(String value) {
        this.value = value;
    }
}
