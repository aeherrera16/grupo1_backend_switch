package com.banquito.core.dto;

import com.banquito.core.enums.CommonStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerAuthResponseDTO {

    private Integer credentialId;
    private Integer customerId;
    private String username;
    private String customerName;
    private CommonStatusEnum status;
    private LocalDateTime lastLogin;

    public CustomerAuthResponseDTO() {
    }

    public CustomerAuthResponseDTO(Integer credentialId, Integer customerId, String username,
                                   String customerName, CommonStatusEnum status, LocalDateTime lastLogin) {
        this.credentialId = credentialId;
        this.customerId = customerId;
        this.username = username;
        this.customerName = customerName;
        this.status = status;
        this.lastLogin = lastLogin;
    }
}
