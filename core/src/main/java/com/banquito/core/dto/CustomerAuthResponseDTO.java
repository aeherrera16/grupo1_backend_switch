package com.banquito.core.dto;

import com.banquito.core.enums.CommonStatusEnum;
import com.banquito.core.enums.CustomerTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerAuthResponseDTO {

    private Integer credentialId;
    private Integer customerId;
    private CustomerTypeEnum customerType;
    private String username;
    private String customerName;
    private String identificationType;
    private String identification;
    private String email;
    private String mobilePhone;
    private String address;
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

    public CustomerAuthResponseDTO(Integer credentialId, Integer customerId, CustomerTypeEnum customerType,
                                   String username, String customerName, String identificationType,
                                   String identification, String email, String mobilePhone, String address,
                                   CommonStatusEnum status, LocalDateTime lastLogin) {
        this.credentialId = credentialId;
        this.customerId = customerId;
        this.customerType = customerType;
        this.username = username;
        this.customerName = customerName;
        this.identificationType = identificationType;
        this.identification = identification;
        this.email = email;
        this.mobilePhone = mobilePhone;
        this.address = address;
        this.status = status;
        this.lastLogin = lastLogin;
    }
}
