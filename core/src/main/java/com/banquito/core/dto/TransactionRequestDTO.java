package com.banquito.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionRequestDTO {

    private String accountNumber;
    private BigDecimal amount;
    private BigDecimal commissionSubtotal;
    private BigDecimal vatAmount;
    private String transactionUuid;
    private String subtypeCode;
    private String description;

    public TransactionRequestDTO() {
    }
}
