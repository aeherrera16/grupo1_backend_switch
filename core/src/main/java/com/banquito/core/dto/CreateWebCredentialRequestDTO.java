package com.banquito.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWebCredentialRequestDTO {

    private Integer customerId;
    private String username;
    private String password;

    public CreateWebCredentialRequestDTO() {
    }
}
