package com.banquito.core.dto;

import com.banquito.core.enums.CustomerStatusEnum;
import com.banquito.core.enums.CustomerTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponseDTO {

    private Integer id;
    private CustomerTypeEnum customerType;
    private String identificationType;
    private String identification;
    private String firstName;
    private String lastName;
    private String email;
    private String mobilePhone;
    private String address;
    private CustomerStatusEnum status;

    public CustomerResponseDTO() {
    }

    public CustomerResponseDTO(Integer id, CustomerTypeEnum customerType, String identificationType,
                               String identification, String firstName, String lastName, String email,
                               String mobilePhone, String address, CustomerStatusEnum status) {
        this.id = id;
        this.customerType = customerType;
        this.identificationType = identificationType;
        this.identification = identification;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobilePhone = mobilePhone;
        this.address = address;
        this.status = status;
    }
}
