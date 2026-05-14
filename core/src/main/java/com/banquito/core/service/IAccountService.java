package com.banquito.core.service;

import java.math.BigDecimal;
import java.util.List;

import com.banquito.core.dto.AccountRequestDTO;
import com.banquito.core.dto.AccountResponseDTO;
import com.banquito.core.dto.BalanceDTO;
import com.banquito.core.dto.TransactionResponseDTO;

public interface IAccountService {

    AccountResponseDTO findByAccountNumber(String accountNumber, Integer coreUserId);

    List<AccountResponseDTO> findByCustomerId(Integer customerId, Integer coreUserId);

    AccountResponseDTO create(AccountRequestDTO request, Integer coreUserId);

    AccountResponseDTO inactivate(String accountNumber, Integer coreUserId);

    AccountResponseDTO block(String accountNumber, Integer coreUserId);

    AccountResponseDTO suspend(String accountNumber, Integer coreUserId);

    BalanceDTO getBalance(String accountNumber);

    TransactionResponseDTO debit(String accountNumber, BigDecimal amount);

    TransactionResponseDTO credit(String accountNumber, BigDecimal amount);

    TransactionResponseDTO transfer(String origin, String destination, BigDecimal amount, String uuid);

    AccountResponseDTO getFavoriteAccount();
}
