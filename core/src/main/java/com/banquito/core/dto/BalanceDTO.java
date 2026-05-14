package com.banquito.core.dto;

import com.banquito.core.enums.AccountStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BalanceDTO {

    private String accountNumber;
    private BigDecimal accountingBalance;
    private BigDecimal availableBalance;
    private AccountStatusEnum status;

    public BalanceDTO() {
    }

    public BalanceDTO(String accountNumber,
                      BigDecimal accountingBalance,
                      BigDecimal availableBalance,
                      AccountStatusEnum status) {
        this.accountNumber = accountNumber;
        this.accountingBalance = accountingBalance;
        this.availableBalance = availableBalance;
        this.status = status;
    }

}
