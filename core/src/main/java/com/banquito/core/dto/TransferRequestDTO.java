package com.banquito.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequestDTO {

    private String originAccountNumber;
    private String destinationAccountNumber;
    private String beneficiaryIdentification;
    private BigDecimal amount;
    private String transactionUuid;
    private String subtypeCode;
    private String description;

    public TransferRequestDTO() {
    }
}
