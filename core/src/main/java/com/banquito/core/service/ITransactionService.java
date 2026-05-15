package com.banquito.core.service;

import com.banquito.core.dto.TransactionResponseDTO;
import com.banquito.core.dto.TransactionHistoryDTO;
import java.math.BigDecimal;
import java.util.List;

public interface ITransactionService {

    TransactionResponseDTO debit(String accountNumber, BigDecimal amount, String uuid, String subtypeCode, String description);

    TransactionResponseDTO credit(String accountNumber, BigDecimal amount, String uuid, String subtypeCode, String description);

    TransactionResponseDTO transfer(String originAccount, String destinationAccount, BigDecimal amount,
                                      String uuid, String subtypeCode, String description);

    List<TransactionHistoryDTO> getTransactionHistory(String accountNumber);
}
