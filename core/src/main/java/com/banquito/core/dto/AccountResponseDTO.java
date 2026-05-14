package com.banquito.core.dto;

import com.banquito.core.enums.AccountStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AccountResponseDTO {

    private Integer id;
    private String accountNumber;
    private String customerFullName;
    private String branchName;
    private String accountSubtypeDescription;
    private AccountStatusEnum status;
    private BigDecimal accountingBalance;
    private BigDecimal availableBalance;
    private Boolean isFavorite;
    private LocalDateTime openingDate;

    public AccountResponseDTO() {
    }

    public AccountResponseDTO(Integer id, String accountNumber, String customerFullName, String branchName,
                              String accountSubtypeDescription, AccountStatusEnum status,
                              BigDecimal accountingBalance, BigDecimal availableBalance,
                              Boolean isFavorite, LocalDateTime openingDate) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.customerFullName = customerFullName;
        this.branchName = branchName;
        this.accountSubtypeDescription = accountSubtypeDescription;
        this.status = status;
        this.accountingBalance = accountingBalance;
        this.availableBalance = availableBalance;
        this.isFavorite = isFavorite;
        this.openingDate = openingDate;
    }
}
