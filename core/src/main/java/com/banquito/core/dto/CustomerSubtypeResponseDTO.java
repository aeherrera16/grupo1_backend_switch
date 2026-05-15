package com.banquito.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerSubtypeResponseDTO {

    private Integer id;
    private String customerType;
    private String name;
    private String description;
    private String status;
    private String observations;

    public CustomerSubtypeResponseDTO() {
    }

    public CustomerSubtypeResponseDTO(Integer id, String customerType, String name,
            String description, String status, String observations) {
        this.id = id;
        this.customerType = customerType;
        this.name = name;
        this.description = description;
        this.status = status;
        this.observations = observations;
    }
}
