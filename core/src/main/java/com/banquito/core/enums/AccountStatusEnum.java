package com.banquito.core.enums;

import lombok.Getter;

@Getter
public enum AccountStatusEnum {

    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO"),
    BLOQUEADO("BLOQUEADO"),
    SUSPENDIDO("SUSPENDIDO");

    private final String value;

    AccountStatusEnum(String value) {
        this.value = value;
    }
}
