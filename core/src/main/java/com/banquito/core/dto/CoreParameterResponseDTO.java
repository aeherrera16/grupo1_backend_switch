package com.banquito.core.dto;

public class CoreParameterResponseDTO {

    private String code;
    private String valueString;

    public CoreParameterResponseDTO() {
    }

    public CoreParameterResponseDTO(String code, String valueString) {
        this.code = code;
        this.valueString = valueString;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }
}
