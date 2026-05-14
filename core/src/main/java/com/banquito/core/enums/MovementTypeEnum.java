package com.banquito.core.enums;

import lombok.Getter;

@Getter
public enum MovementTypeEnum {

    DEBITO("DEBITO"),
    CREDITO("CREDITO");

    private final String value;

    MovementTypeEnum(String value) {
        this.value = value;
    }
}
