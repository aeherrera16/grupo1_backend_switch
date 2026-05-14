package com.banquito.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchRequestDTO {

    private String branchCode;
    private String name;
    private String city;

    public BranchRequestDTO() {
    }

    public BranchRequestDTO(String branchCode, String name, String city) {
        this.branchCode = branchCode;
        this.name = name;
        this.city = city;
    }
}
