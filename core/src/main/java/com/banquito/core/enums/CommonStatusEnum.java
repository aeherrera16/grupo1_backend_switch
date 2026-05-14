package com.banquito.core.enums;

import lombok.Getter;

@Getter
public enum CommonStatusEnum {
    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO"),
    BLOQUEADO("BLOQUEADO");

    private final String value;

    CommonStatusEnum(String value) {
        this.value = value;
    }
}
