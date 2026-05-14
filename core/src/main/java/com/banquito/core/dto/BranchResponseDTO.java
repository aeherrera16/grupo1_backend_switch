package com.banquito.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchResponseDTO {

    private Integer id;
    private String branchCode;
    private String name;
    private String city;

    public BranchResponseDTO() {
    }

    public BranchResponseDTO(Integer id, String branchCode, String name, String city) {
        this.id = id;
        this.branchCode = branchCode;
        this.name = name;
        this.city = city;
    }
}
