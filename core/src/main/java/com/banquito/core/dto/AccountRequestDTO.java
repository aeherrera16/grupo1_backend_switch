package com.banquito.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountRequestDTO {

    private String accountNumber;
    private Integer customerId;
    private Integer branchId;
    private Integer accountSubtypeId;
    private Boolean isFavorite;
    private BigDecimal initialBalance;

    public AccountRequestDTO() {
    }

    public AccountRequestDTO(String accountNumber, Integer customerId, Integer branchId,
                             Integer accountSubtypeId, Boolean isFavorite, BigDecimal initialBalance) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.branchId = branchId;
        this.accountSubtypeId = accountSubtypeId;
        this.isFavorite = isFavorite;
        this.initialBalance = initialBalance;
    }
}
