package com.banquito.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.core.dto.BalanceDTO;
import com.banquito.core.dto.CoreParameterResponseDTO;
import com.banquito.core.dto.TransactionRequestDTO;
import com.banquito.core.dto.TransferRequestDTO;
import com.banquito.core.dto.TransferResultDTO;
import com.banquito.core.integration.CoreSwitchService;
import com.banquito.core.repository.CoreParameterRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/core/v1/integration")
@RequiredArgsConstructor
public class CoreIntegrationController {

    private final CoreSwitchService coreSwitchService;
    private final CoreParameterRepository coreParameterRepository;

    @GetMapping("/balance/{accountNumber}")
    public ResponseEntity<BalanceDTO> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(coreSwitchService.getBalance(accountNumber));
    }

    @GetMapping("/account/{accountNumber}/valid")
    public ResponseEntity<Boolean> isAccountValid(@PathVariable String accountNumber) {
        return ResponseEntity.ok(coreSwitchService.validateAccount(accountNumber));
    }

    /**
     * RF-02 Switch: alta de clientes empresa con pagos masivos modelada mediante subtipo {@code EMPRESA_PAGOS_MASIVOS}.
     */
    @GetMapping("/customer/mass-payments/{ruc}/active")
    public ResponseEntity<Boolean> isMassPaymentsActive(@PathVariable String ruc) {
        return ResponseEntity.ok(coreSwitchService.isMassPaymentsActiveForRuc(ruc));
    }

    @GetMapping("/parameter/{code}")
    public ResponseEntity<CoreParameterResponseDTO> getParameter(@PathVariable String code) {
        return coreParameterRepository.findByCode(code)
                .map(parameter -> ResponseEntity.ok(new CoreParameterResponseDTO(
                        parameter.getCode(),
                        parameter.getValueString())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResultDTO> transfer(@RequestBody TransferRequestDTO request) {
        return ResponseEntity.ok(coreSwitchService.transfer(
                request.getOriginAccountNumber(),
                request.getDestinationAccountNumber(),
                request.getBeneficiaryIdentification(),
                request.getAmount(),
                request.getTransactionUuid()
        ));
    }

    @PostMapping("/commission")
    public ResponseEntity<TransferResultDTO> chargeCommission(@RequestBody TransactionRequestDTO request) {
        return ResponseEntity.ok(coreSwitchService.chargeCommission(
                request.getAccountNumber(),
                request.getCommissionSubtotal(),
                request.getVatAmount(),
                request.getAmount(),
                request.getTransactionUuid()
        ));
    }
}
