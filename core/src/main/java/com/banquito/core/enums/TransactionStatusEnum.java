package com.banquito.core.enums;

import lombok.Getter;

@Getter
public enum TransactionStatusEnum {

    COMPLETADA("COMPLETADA"),
    RECHAZADA("RECHAZADA");

    private final String value;

    TransactionStatusEnum(String value) {
        this.value = value;
    }
}
