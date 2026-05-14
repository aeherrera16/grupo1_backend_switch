package com.banquito.core.integration;

import com.banquito.core.dto.BalanceDTO;
import com.banquito.core.dto.TransferResultDTO;

import java.math.BigDecimal;

public interface CoreSwitchService {

    BalanceDTO getBalance(String accountNumber);

    boolean validateAccount(String accountNumber);

    /**
     * Indica si el RUC está asociado a un cliente jurídico ACTIVO con subtipo EMPRESA_PAGOS_MASIVOS vigente en Core.
     */
    boolean isMassPaymentsActiveForRuc(String ruc);

    TransferResultDTO transfer(
            String originAccount,
            String destinationAccount,
            String beneficiaryIdentification,
            BigDecimal amount,
            String uuid
    );

    TransferResultDTO chargeCommission(
            String companyAccountNumber,
            BigDecimal commissionSubtotal,
            BigDecimal vatAmount,
            BigDecimal totalAmount,
            String uuid
    );
}
