package com.banquito.core.dto;

import com.banquito.core.enums.MovementTypeEnum;
import com.banquito.core.enums.TransactionStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionResponseDTO {

    private Long id;
    private String accountNumber;
    private MovementTypeEnum movementType;
    private BigDecimal amount;
    private BigDecimal resultingBalance;
    private LocalDateTime transactionDate;
    private String transactionUuid;
    private TransactionStatusEnum status;
    private String message;

    public TransactionResponseDTO() {
    }

    public TransactionResponseDTO(Long id, String accountNumber, MovementTypeEnum movementType,
                                  BigDecimal amount, BigDecimal resultingBalance,
                                  LocalDateTime transactionDate, String transactionUuid,
                                  TransactionStatusEnum status, String message) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.movementType = movementType;
        this.amount = amount;
        this.resultingBalance = resultingBalance;
        this.transactionDate = transactionDate;
        this.transactionUuid = transactionUuid;
        this.status = status;
        this.message = message;
    }
}
