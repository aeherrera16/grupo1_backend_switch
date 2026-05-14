package com.banquito.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCoreUserRequestDTO {

    private String username;
    private String password;
    private String fullName;
    private String role;

    public CreateCoreUserRequestDTO() {
    }
}
